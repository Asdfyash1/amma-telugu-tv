package com.yash.ammatv;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TvDiscovery {
    private static final int ADB_PORT = 5555;

    public static String findTvIp(Context context, String cachedIp) {
        // 1. Check cached IP first
        if (cachedIp != null && !cachedIp.isEmpty() && isPortOpen(cachedIp, ADB_PORT, 350)) {
            return cachedIp;
        }

        // 2. Check known hotspot / home router IPs
        String[] knownIps = new String[]{"10.203.242.129", "10.246.58.129", "192.168.29.129"};
        for (String ip : knownIps) {
            if (isPortOpen(ip, ADB_PORT, 300)) {
                return ip;
            }
        }

        // 3. Try SSDP M-SEARCH (finds Chromecast / Android TV in < 500ms)
        String ssdpHit = discoverViaSsdp();
        if (ssdpHit != null && isPortOpen(ssdpHit, ADB_PORT, 350)) {
            return ssdpHit;
        }

        // 4. Subnet scan on current Wi-Fi / Hotspot /24
        List<String> prefixes = getLocalSubnets(context);
        for (String prefix : prefixes) {
            // Try .129 first (Android hotspot default client IP for your TCL TV!)
            if (isPortOpen(prefix + "129", ADB_PORT, 300)) {
                return prefix + "129";
            }
            String scanned = scanSubnet(prefix);
            if (scanned != null) {
                return scanned;
            }
        }
        return null;
    }

    public static boolean isPortOpen(String ip, int port, int timeoutMs) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(ip, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String discoverViaSsdp() {
        String msg = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: 1\r\n" +
                "ST: urn:dial-multiscreen-org:service:dial:1\r\n\r\n";
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(400);
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pkt = new DatagramPacket(data, data.length,
                    InetAddress.getByName("239.255.255.250"), 1900);
            socket.send(pkt);

            long end = System.currentTimeMillis() + 700;
            byte[] buf = new byte[2048];
            while (System.currentTimeMillis() < end) {
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.receive(resp);
                String host = resp.getAddress().getHostAddress();
                if (host != null && isPortOpen(host, ADB_PORT, 250)) {
                    return host;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static List<String> getLocalSubnets(Context context) {
        List<String> prefixes = new ArrayList<>();
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null && info.getIpAddress() != 0) {
                    int ip = info.getIpAddress();
                    String prefix = String.format("%d.%d.%d.", (ip & 0xff), (ip >> 8 & 0xff), (ip >> 16 & 0xff));
                    prefixes.add(prefix);
                }
            }
        } catch (Exception ignored) {
        }

        try {
            for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!nif.isUp() || nif.isLoopback()) continue;
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        String ip = addr.getHostAddress();
                        if (ip != null && ip.contains(".")) {
                            String prefix = ip.substring(0, ip.lastIndexOf('.') + 1);
                            if (!prefixes.contains(prefix)) {
                                prefixes.add(prefix);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return prefixes;
    }

    private static String scanSubnet(final String prefix) {
        ExecutorService pool = Executors.newFixedThreadPool(48);
        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 1; i <= 254; i++) {
            final String ip = prefix + i;
            tasks.add(() -> isPortOpen(ip, ADB_PORT, 250) ? ip : null);
        }
        try {
            List<Future<String>> results = pool.invokeAll(tasks, 2500, TimeUnit.MILLISECONDS);
            for (Future<String> f : results) {
                if (f.isDone() && !f.isCancelled()) {
                    try {
                        String hit = f.get();
                        if (hit != null) {
                            pool.shutdownNow();
                            return hit;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        } finally {
            pool.shutdownNow();
        }
        return null;
    }
}
