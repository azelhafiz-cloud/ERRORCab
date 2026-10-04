package com.errorcab;

import com.errorcab.copilot.service.CopilotRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification of Copilot Rate Limiting.
 * Enforces:
 * - 10 requests / 10 minutes per IP
 * - 20 requests / 10 minutes per authenticated user
 */
public class RateLimiterTest {

    private CopilotRateLimiter rateLimiter;

    @BeforeEach
    public void setUp() {
        rateLimiter = new CopilotRateLimiter();
    }

    @Test
    public void testIpRateLimiting() {
        String testIp = "192.168.1.100";

        // First 10 requests should succeed
        for (int i = 1; i <= 10; i++) {
            boolean allowed = rateLimiter.tryAcquire(testIp, null);
            assertTrue(allowed, "Request " + i + " should be allowed");
        }

        // 11th request must be rejected
        boolean blocked = rateLimiter.tryAcquire(testIp, null);
        assertFalse(blocked, "11th request from same IP must be rate limited");
    }

    @Test
    public void testUserRateLimiting() {
        Integer testUserId = 42;

        // First 20 requests should succeed (using distinct IPs so IP limit doesn't trip first)
        for (int i = 1; i <= 20; i++) {
            String ip = "10.0.0." + (i % 8); // keeps each IP under 10
            boolean allowed = rateLimiter.tryAcquire(ip, testUserId);
            assertTrue(allowed, "User request " + i + " should be allowed");
        }

        // 21st request for the same user must be rejected
        boolean blocked = rateLimiter.tryAcquire("10.0.0.99", testUserId);
        assertFalse(blocked, "21st request for same user must be rate limited");
    }

    @Test
    public void testResetClearsWindow() {
        String testIp = "172.16.0.1";

        for (int i = 0; i < 10; i++) {
            rateLimiter.tryAcquire(testIp, null);
        }
        assertFalse(rateLimiter.tryAcquire(testIp, null));

        // Reset
        rateLimiter.reset();

        // Allowed again
        assertTrue(rateLimiter.tryAcquire(testIp, null));
    }
}
