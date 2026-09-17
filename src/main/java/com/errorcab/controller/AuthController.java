package com.errorcab.controller;

import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.User;
import com.errorcab.service.AuthenticationService;
import com.errorcab.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST Controller for User Authentication, Registration, and Secure Sessions.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authService;
    private final SessionService sessionService;

    public AuthController(AuthenticationService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            User user = authService.login(email, password);

            String sessionToken = sessionService.createSession(user);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", user.getId());
            resp.put("userId", user.getId());
            resp.put("name", user.getName());
            resp.put("email", user.getEmail());
            resp.put("phone", user.getPhone());
            resp.put("role", user.getRole().name());
            resp.put("active", user.isActive());
            resp.put("sessionToken", sessionToken);
            resp.put("user", user);

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "An error occurred during login."));
        }
    }

    @PostMapping("/register/passenger")
    public ResponseEntity<?> registerPassenger(@RequestBody Map<String, String> body) {
        try {
            String pass = body.get("password");
            String confirmPass = body.get("confirmPassword");
            if (confirmPass == null || confirmPass.trim().isEmpty()) {
                confirmPass = pass;
            }

            Passenger p = authService.registerPassenger(
                    body.get("name"),
                    body.get("email"),
                    body.get("phone"),
                    pass,
                    confirmPass,
                    body.get("address")
            );

            String sessionToken = sessionService.createSession(p);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", p.getId());
            resp.put("userId", p.getId());
            resp.put("name", p.getName());
            resp.put("email", p.getEmail());
            resp.put("phone", p.getPhone());
            resp.put("role", p.getRole().name());
            resp.put("active", p.isActive());
            resp.put("sessionToken", sessionToken);
            resp.put("user", p);

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/driver")
    public ResponseEntity<?> registerDriver(@RequestBody Map<String, String> body) {
        try {
            String pass = body.get("password");
            String confirmPass = body.get("confirmPassword");
            if (confirmPass == null || confirmPass.trim().isEmpty()) {
                confirmPass = pass;
            }

            String plate = body.get("plateNumber") != null ? body.get("plateNumber") : body.get("licensePlate");

            Driver d = authService.registerDriver(
                    body.get("name"),
                    body.get("email"),
                    body.get("phone"),
                    pass,
                    confirmPass,
                    body.get("licenseNumber"),
                    body.get("vehicleModel"),
                    plate,
                    body.get("cabType"),
                    body.get("color")
            );

            String sessionToken = sessionService.createSession(d);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", d.getId());
            resp.put("userId", d.getId());
            resp.put("name", d.getName());
            resp.put("email", d.getEmail());
            resp.put("phone", d.getPhone());
            resp.put("role", d.getRole().name());
            resp.put("active", d.isActive());
            resp.put("sessionToken", sessionToken);
            resp.put("user", d);

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        sessionService.getSessionFromRequest(request)
                .ifPresent(s -> sessionService.invalidateSession(s.token()));
        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        return sessionService.resolveUser(request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable int id, HttpServletRequest request) {
        if (!sessionService.isUserOrAdmin(request, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied. You can only view your own profile."));
        }
        return authService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        var userOpt = sessionService.resolveUser(request);
        int userId;
        if (userOpt.isPresent()) {
            userId = userOpt.get().getId();
        } else if (body.containsKey("userId")) {
            userId = Integer.parseInt(body.get("userId"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required."));
        }

        String name = body.get("name");
        String phone = body.get("phone");
        String address = body.get("address") != null ? body.get("address") : body.get("defaultAddress");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name cannot be empty."));
        }
        if (phone == null || phone.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number cannot be empty."));
        }

        boolean ok = new com.errorcab.repository.UserRepository().updateUserProfile(userId, name.trim(), phone.trim(), address);
        if (ok) {
            var updatedUser = authService.getUserById(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully.", "user", updatedUser.orElse(null)));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to update profile."));
        }
    }
}
