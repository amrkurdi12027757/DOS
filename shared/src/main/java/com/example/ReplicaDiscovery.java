package com.example;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ReplicaDiscovery {
    private final Map<String, AtomicReference<List<String>>> cachedPeers = new ConcurrentHashMap<>();

    private ReplicaDiscovery() {
    }

    private static class Holder {
        private static final ReplicaDiscovery INSTANCE = new ReplicaDiscovery();
    }

    public static ReplicaDiscovery getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Get all peer IPs of the given DNSRR service, excluding the current container.
     */
    public List<String> getOthers(String dnsrrHost) {
        return getAll(dnsrrHost).stream()
                .filter(ip -> !ip.equals(getMyIpSafe()))
                .collect(Collectors.toList());
    }

    /**
     * Get all IPs (including self) of the given DNSRR service from cache or resolve.
     */
    public List<String> getAll(String dnsrrHost) {
        return cachedPeers
                .computeIfAbsent(dnsrrHost, k -> new AtomicReference<>())
                .updateAndGet(existing -> (existing != null) ? existing : resolve(dnsrrHost));
    }

    /**
     * Force DNS re-resolution for the given DNSRR service and update the cache.
     */
    public List<String> refresh(String dnsrrHost) {
        List<String> resolved = resolve(dnsrrHost);
        cachedPeers.computeIfAbsent(dnsrrHost, k -> new AtomicReference<>()).set(resolved);
        return resolved;
    }

    /**
     * Force DNS re-resolution for the All DNSRR service and update the cache.
     */
    public void refreshAll() {
        for (String dnsrrHost : cachedPeers.keySet()) {
            List<String> resolved = resolve(dnsrrHost);
            cachedPeers.put(dnsrrHost, new AtomicReference<>(resolved));
        }
    }


    private List<String> resolve(String dnsrrHost) {
        int retries = 20;
        int delayMs = 1000;

        for (int i = 0; i < retries; i++) {
            try {
                InetAddress[] all = InetAddress.getAllByName(dnsrrHost);
                return Arrays.stream(all)
                        .map(InetAddress::getHostAddress)
                        .distinct()
                        .collect(Collectors.toList());
            } catch (UnknownHostException e) {
                if (i == retries - 1) {
                    throw new RuntimeException("Failed to resolve peers for host: " + dnsrrHost, e);
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {
                }
            }
        }
        return List.of(); // unreachable
    }


    private String getMyIpSafe() {
        try {
            for (NetworkInterface nic : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(nic.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Failed to get local IP", e);
        }
        throw new IllegalStateException("No valid non-loopback IP found");
    }
}
