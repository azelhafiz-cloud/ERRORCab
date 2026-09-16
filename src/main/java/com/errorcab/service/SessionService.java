package com.errorcab.service;

import com.errorcab.model.Role;
import com.errorcab.model.User;
import com.errorcab.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing cryptographically secure server-side sessions and token-based authorization.
 */
@Service
public class SessionService {

    public record UserSession(
            String token,
            int userId,
            String email,
            String name,
            Role role,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final UserRepository userRepository = new UserRepository();
    private final SecureRandom secureRandom = new SecureRandom();
    private static final int SESSION_DURATION_HOURS = 24;

    /**
     * Creates an active server-side session for an authenticated user.
     */
    public String createSession(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(SESSION_DURATION_HOURS);

        UserSession session = new UserSession(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                now,
                expiresAt
        );

        activeSessions.put(token, session);
        return token;
    }

    /**
     * Retrieves an active session if valid and not expired.
     */
    public Optional<UserSession> getSession(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }

        UserSession session = activeSessions.get(token.trim());
        if (session == null) {
            return Optional.empty();
        }

        if (session.isExpired()) {
            activeSessions.remove(token.trim());
            return Optional.empty();
        }

        return Optional.of(session);
    }

    /**
     * Invalidates a session upon user logout.
     */
    public void invalidateSession(String token) {
        if (token != null) {
            activeSessions.remove(token.trim());
        }
    }

    /**
     * Resolves the session from incoming HTTP request headers (Authorization: Bearer <token> or X-Session-Token).
     */
    public Optional<UserSession> getSessionFromRequest(HttpServletRequest request) {
        if (request == null) return Optional.empty();

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = authHeader.substring(7).trim();
        } else if (authHeader != null && !authHeader.trim().isEmpty()) {
            token = authHeader.trim();
        }

        if (token == null || token.isEmpty()) {
            token = request.getHeader("X-Session-Token");
        }

        return getSession(token);
    }

    /**
     * Resolves the full User entity from the incoming request.
     */
    public Optional<User> resolveUser(HttpServletRequest request) {
        return getSessionFromRequest(request)
                .flatMap(s -> userRepository.findById(s.userId()));
    }

    /**
     * Validates whether the incoming request belongs to an authenticated Admin.
     */
    public boolean isAdmin(HttpServletRequest request) {
        return getSessionFromRequest(request)
                .map(s -> s.role() == Role.ADMIN)
                .orElse(false);
    }

    /**
     * Validates whether the incoming request belongs to the resource owner or an Admin.
     */
    public boolean isUserOrAdmin(HttpServletRequest request, int targetUserId) {
        return getSessionFromRequest(request)
                .map(s -> s.userId() == targetUserId || s.role() == Role.ADMIN)
                .orElse(false);
    }
}
