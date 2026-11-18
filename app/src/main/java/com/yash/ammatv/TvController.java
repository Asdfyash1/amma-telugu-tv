package com.yash.ammatv;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TvController {
    private static final int ADB_PORT = 5555;
    private static final Pattern SEARCH_ITEM_PATTERN = Pattern.compile(
            "resource-id=\"studio\\.scillarium\\.ottnavigator:id/search_item_title\"[^>]*text=\"([^\"]+)\"|text=\"([^\"]+)\"[^>]*resource-id=\"studio\\.scillarium\\.ottnavigator:id/search_item_title\""
    );

    public static class PlayResult {
        public final boolean success;
        public final String message;
        public final String matchedTitle;

        public PlayResult(boolean success, String message, String matchedTitle) {
            this.success = success;
            this.message = message;
            this.matchedTitle = matchedTitle;
        }
    }

    public static PlayResult playChannel(AdbClient adb, String tvIp, ChannelItem channel) {
        try {
            String safeQuery = channel.searchQuery.replace("'", "");

            // 1. Check if OTT Navigator MainActivity is currently resumed in foreground
            String state = adb.runShellCommand(tvIp, ADB_PORT,
                    "dumpsys activity activities | grep mResumedActivity", 4000);
            boolean isOttInForeground = (state != null && state.contains("studio.scillarium.ottnavigator"));

            if (!isOttInForeground) {
                // Launch OTT Navigator to foreground first and wait until its fragment is attached & resumed!
                // (In OTT Navigator's MainActivity.onCreate, ACTION_SEARCH is ignored if fragment c is not yet attached!)
                adb.runShellCommand(tvIp, ADB_PORT,
                        "am start -a android.intent.action.MAIN -c android.intent.category.LEANBACK_LAUNCHER " +
                                "-n studio.scillarium.ottnavigator/.MainActivity >/dev/null 2>&1", 4000);

                // Poll until OTT Navigator is resumed and wait 2.2s for its home fragment to attach
                for (int attempt = 0; attempt < 6; attempt++) {
                    Thread.sleep(500);
                    String check = adb.runShellCommand(tvIp, ADB_PORT,
                            "dumpsys activity activities | grep mResumedActivity", 3000);
                    if (check != null && check.contains("studio.scillarium.ottnavigator")) {
                        break;
                    }
                }
                Thread.sleep(2200);
            }

            // 2. Now send ACTION_SEARCH to the already-resumed MainActivity (triggers onNewIntent -> opens Lsh/r2 search dialog)
            // Poll up to 3 times until search_root_layer or field_search_query appears in uiautomator dump!
            String xml = null;
            for (int trySearch = 0; trySearch < 3; trySearch++) {
                String searchCmd = "am start -a android.intent.action.SEARCH --es query '" + safeQuery + "' " +
                        "-n studio.scillarium.ottnavigator/.MainActivity >/dev/null 2>&1; " +
                        "sleep 1.5; " +
                        "rm -f /sdcard/ammatv_ui.xml; " +
                        "uiautomator dump /sdcard/ammatv_ui.xml >/dev/null 2>&1; " +
                        "cat /sdcard/ammatv_ui.xml";
                xml = adb.runShellCommand(tvIp, ADB_PORT, searchCmd, 9000);

                if (xml != null && (xml.contains("search_root_layer") || xml.contains("field_search_query"))) {
                    // Search overlay is definitely open!
                    // If channels are still loading ("Please wait while OTT loads"), wait a bit and re-dump
                    if (!xml.contains("search_item_title") && !xml.contains("search_no_data")) {
                        Thread.sleep(1500);
                        xml = adb.runShellCommand(tvIp, ADB_PORT,
                                "rm -f /sdcard/ammatv_ui.xml; uiautomator dump /sdcard/ammatv_ui.xml >/dev/null 2>&1; cat /sdcard/ammatv_ui.xml", 6000);
                    }
                    break;
                }
                // Fragment wasn't ready yet — wait 1.5s and fire ACTION_SEARCH again
                Thread.sleep(1500);
            }

            if (xml == null || (!xml.contains("search_root_layer") && !xml.contains("field_search_query"))) {
                return new PlayResult(false, "OTT Navigator is still loading — tap " + channel.title + " again", null);
            }

            if (xml.contains("search_no_data")) {
                adb.runShellCommand(tvIp, ADB_PORT, "input keyevent KEYCODE_BACK", 3000);
                return new PlayResult(false, channel.title + " is not in the current M3U playlist", null);
            }

            List<String> titles = extractSearchTitles(xml);
            if (titles.isEmpty()) {
                // Wait 1.5s more for playlist cache to populate search results
                String retryXml = adb.runShellCommand(tvIp, ADB_PORT,
                        "sleep 1.5; rm -f /sdcard/ammatv_ui.xml; uiautomator dump /sdcard/ammatv_ui.xml >/dev/null 2>&1; cat /sdcard/ammatv_ui.xml", 6000);
                if (retryXml != null && retryXml.contains("search_no_data")) {
                    adb.runShellCommand(tvIp, ADB_PORT, "input keyevent KEYCODE_BACK", 3000);
                    return new PlayResult(false, channel.title + " is not in the current M3U playlist", null);
                }
                titles = extractSearchTitles(retryXml);
            }

            if (titles.isEmpty()) {
                return new PlayResult(false, "Playlist still loading — tap " + channel.title + " again", null);
            }

            int bestIndex = pickBestMatchIndex(titles, channel);
            if (bestIndex < 0) {
                adb.runShellCommand(tvIp, ADB_PORT, "input keyevent KEYCODE_BACK", 3000);
                return new PlayResult(false, channel.title + " is not in the current M3U playlist", null);
            }

            String matched = cleanTitle(titles.get(bestIndex));
            // Navigate from search box (focused) -> filter row (1st DOWN) -> result #bestIndex (1 + bestIndex DOWNs) -> CENTER
            StringBuilder nav = new StringBuilder();
            int totalDowns = 2 + bestIndex;
            for (int i = 0; i < totalDowns; i++) {
                nav.append("input keyevent KEYCODE_DPAD_DOWN; sleep 0.3; ");
            }
            nav.append("input keyevent KEYCODE_DPAD_CENTER");

            adb.runShellCommand(tvIp, ADB_PORT, nav.toString(), 7000);
            return new PlayResult(true, "Playing " + matched, matched);
        } catch (Exception e) {
            return new PlayResult(false, "TV connection error: " + e.getMessage(), null);
        }
    }

    private static List<String> extractSearchTitles(String xml) {
        List<String> list = new ArrayList<>();
        if (xml == null || xml.isEmpty()) return list;
        Matcher m = SEARCH_ITEM_PATTERN.matcher(xml);
        while (m.find()) {
            String t = m.group(1) != null ? m.group(1) : m.group(2);
            if (t != null && !t.isEmpty()) {
                list.add(t);
            }
        }
        return list;
    }

    private static int pickBestMatchIndex(List<String> titles, ChannelItem channel) {
        if (titles.isEmpty()) return -1;

        int bestIdx = -1;
        int bestScore = -1;

        for (int i = 0; i < titles.size(); i++) {
            String raw = titles.get(i).toUpperCase(Locale.ROOT);
            int dashIdx = raw.indexOf("—");
            String namePart = dashIdx > 0 ? raw.substring(0, dashIdx).trim() : raw;

            boolean excluded = false;
            for (String ex : channel.mustNotContain) {
                if (namePart.contains(ex.toUpperCase(Locale.ROOT))) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) continue;

            int score = 0;
            boolean matchedRequired = (channel.mustContainAny.length == 0);
            for (String req : channel.mustContainAny) {
                String reqUp = req.toUpperCase(Locale.ROOT);
                if (namePart.contains(reqUp)) {
                    matchedRequired = true;
                    score += 50;
                    if (namePart.endsWith(reqUp) || namePart.endsWith(reqUp + " HD") || namePart.endsWith(reqUp + " FHD")) {
                        score += 30;
                    }
                    break;
                }
            }
            if (!matchedRequired) continue;

            if (namePart.contains("TELEGU") || namePart.contains("TELUGU")) {
                score += 20;
            }
            if (namePart.contains("HD")) {
                score += 10;
            }

            if (score > bestScore) {
                bestScore = score;
                bestIdx = i;
            }
        }

        return bestIdx;
    }

    private static String cleanTitle(String raw) {
        int dash = raw.indexOf("—");
        if (dash > 0) {
            return raw.substring(0, dash).trim();
        }
        return raw.trim();
    }
}
