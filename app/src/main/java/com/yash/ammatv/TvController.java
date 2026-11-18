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
            // 1. Open OTT Navigator search with query and dump UI hierarchy
            String cmd = "am start -a android.intent.action.SEARCH --es query '" + safeQuery + "' " +
                    "-n studio.scillarium.ottnavigator/.MainActivity >/dev/null 2>&1; " +
                    "sleep 1.4; " +
                    "uiautomator dump /sdcard/ammatv_ui.xml >/dev/null 2>&1; " +
                    "cat /sdcard/ammatv_ui.xml";

            String xml = adb.runShellCommand(tvIp, ADB_PORT, cmd, 8000);

            if (xml != null && xml.contains("search_no_data")) {
                // Dismiss search overlay if nothing matched
                adb.runShellCommand(tvIp, ADB_PORT, "input keyevent KEYCODE_BACK", 3000);
                return new PlayResult(false, channel.title + " is not in the current M3U playlist", null);
            }

            List<String> titles = extractSearchTitles(xml);
            if (titles.isEmpty()) {
                // Retry dump once after 1 second in case OTT Navigator was cold-starting
                String retryXml = adb.runShellCommand(tvIp, ADB_PORT,
                        "sleep 1.2; uiautomator dump /sdcard/ammatv_ui.xml >/dev/null 2>&1; cat /sdcard/ammatv_ui.xml", 6000);
                if (retryXml != null && retryXml.contains("search_no_data")) {
                    adb.runShellCommand(tvIp, ADB_PORT, "input keyevent KEYCODE_BACK", 3000);
                    return new PlayResult(false, channel.title + " is not in the current M3U playlist", null);
                }
                titles = extractSearchTitles(retryXml);
            }

            int bestIndex = pickBestMatchIndex(titles, channel);
            if (bestIndex < 0) {
                if (titles.isEmpty()) {
                    // Fallback if uiautomator didn't return XML: select first result
                    String fallbackCmd = "input keyevent KEYCODE_DPAD_DOWN; sleep 0.35; input keyevent KEYCODE_DPAD_DOWN; sleep 0.35; input keyevent KEYCODE_DPAD_CENTER";
                    adb.runShellCommand(tvIp, ADB_PORT, fallbackCmd, 5000);
                    return new PlayResult(true, "Playing " + channel.title, channel.title);
                } else {
                    adb.runShellCommand(tvIp, ADB_PORT, "input keyevent KEYCODE_BACK", 3000);
                    return new PlayResult(false, channel.title + " is not in the current M3U playlist", null);
                }
            }

            String matched = cleanTitle(titles.get(bestIndex));
            // Navigate from search box -> filter row (1 DOWN) -> item #bestIndex (1 + bestIndex DOWNs) -> CENTER
            StringBuilder nav = new StringBuilder();
            int totalDowns = 2 + bestIndex;
            for (int i = 0; i < totalDowns; i++) {
                nav.append("input keyevent KEYCODE_DPAD_DOWN; sleep 0.25; ");
            }
            nav.append("input keyevent KEYCODE_DPAD_CENTER");

            adb.runShellCommand(tvIp, ADB_PORT, nav.toString(), 6000);
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
            // Strip " — NO EPG TELEGUIDE" suffix for clean matching
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

            // Bonus if it has TELEGU or TELUGU tag
            if (namePart.contains("TELEGU") || namePart.contains("TELUGU")) {
                score += 20;
            }
            // Prefer HD
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
