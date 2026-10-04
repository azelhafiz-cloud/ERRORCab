package com.errorcab.copilot.service;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Demo-safe sliding window rate limiter for /api/ai/copilot/plan endpoints.
 * Enforces:
 * - 10 requests per 10 minutes per IP
 * - 20 requests per 10 minutes per authenticated user
 */
@Component
public class CopilotRateLimiter {

    public static final int IP_LIMIT = 10;
    public static final int USER_LIMIT = 20;
    public static final long WINDOW_MS = 10 * 60 * 1000L; // 10 minutes

    private final Map<String, LinkedList<Long>> ipRequests = new ConcurrentHashMap<>();
    private final Map<Integer, LinkedList<Long>> userRequests = new ConcurrentHashMap<>();

    public synchronized boolean tryAcquire(String ip, Integer userId) {
        long now = System.currentTimeMillis();

        // 1. Check IP limit
        if (ip != null && !ip.isBlank()) {
            LinkedList<Long> timestamps = ipRequests.computeIfAbsent(ip, k -> new LinkedList<>());
            evictOlderThan(timestamps, now - WINDOW_MS);
            if (timestamps.size() >= IP_LIMIT) {
                return false;
            }
        }

        // 2. Check User limit
        if (userId != null && userId > 0) {
            LinkedList<Long> timestamps = userRequests.computeIfAbsent(userId, k -> new LinkedList<>());
            evictOlderThan(timestamps, now - WINDOW_MS);
            if (timestamps.size() >= USER_LIMIT) {
                return false;
            }
        }

        // Record request
        if (ip != null && !ip.isBlank()) {
            ipRequests.get(ip).addLast(now);
        }
        if (userId != null && userId > 0) {
            userRequests.get(userId).addLast(now);
        }

        return true;
    }

    private void evictOlderThan(LinkedList<Long> list, long cutoff) {
        while (!list.isEmpty() && list.peekFirst() < cutoff) {
            list.pollFirst();
        }
    }

    public synchronized void reset() {
        ipRequests.clear();
        userRequests.clear();
    }
}
