package com.errorcab.service;

import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.User;
import com.errorcab.repository.UserRepository;
import com.errorcab.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service managing user authentication, registration with password hashing, and role checks.
 */
@Service
public class AuthenticationService {
    private final UserRepository userRepo = new UserRepository();

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        Optional<User> userOpt = userRepo.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("No account found with email: " + email);
        }

        User user = userOpt.get();
        if (!PasswordUtil.verify(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password. Please try again.");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("Your account has been deactivated by the Admin. Please contact support.");
        }

        return user;
    }

    public Passenger registerPassenger(String name, String email, String phone, String password, String confirmPassword, String address) throws SQLException {
        validateRegistration(name, email, phone, password, confirmPassword);

        if (userRepo.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = PasswordUtil.hash(password);
        return userRepo.registerPassenger(name.trim(), email.trim(), phone.trim(), hashedPassword, address);
    }

    public Driver registerDriver(String name, String email, String phone, String password, String confirmPassword,
                                 String license, String vehicleModel, String plateNumber, String cabType, String color) throws SQLException {
        validateRegistration(name, email, phone, password, confirmPassword);

        if (license == null || license.trim().isEmpty()) {
            throw new IllegalArgumentException("Driving License number is required.");
        }
        if (vehicleModel == null || vehicleModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle model is required.");
        }
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle registration number is required (e.g. KL 07 AB 1234).");
        }
        if (userRepo.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = PasswordUtil.hash(password);
        return userRepo.registerDriver(name.trim(), email.trim(), phone.trim(), hashedPassword,
                license.trim().toUpperCase(), vehicleModel.trim(), plateNumber.trim().toUpperCase(), cabType, color);
    }

    public Optional<User> getUserById(int id) {
        return userRepo.findById(id);
    }

    private void validateRegistration(String name, String email, String phone, String password, String confirmPassword) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Full name must be at least 2 characters.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (phone == null || phone.trim().length() < 10) {
            throw new IllegalArgumentException("Please enter a valid Indian mobile number (e.g. +91 98471 23456).");
        }
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }
        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }
}
