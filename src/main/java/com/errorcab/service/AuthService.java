package com.errorcab.service;

import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.User;
import com.errorcab.repository.UserRepository;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service managing user authentication, sessions, and role checks.
 */
public class AuthService {
    private static AuthService instance;
    private final UserRepository userRepository = new UserRepository();
    private User currentUser;

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public Optional<User> login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your email address");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter your password");
        }

        Optional<User> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("No account found with email: " + email);
        }

        User user = userOpt.get();
        if (!com.errorcab.util.PasswordUtil.verify(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password. Please try again.");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("Your account has been deactivated by the Admin. Please contact support.");
        }

        this.currentUser = user;
        return Optional.of(user);
    }

    public Passenger registerPassenger(String name, String email, String phone, String password, String confirmPassword, String address) throws Exception {
        validateCommonRegistration(name, email, phone, password, confirmPassword);

        if (userRepository.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = com.errorcab.util.PasswordUtil.hash(password);
        Passenger passenger = userRepository.registerPassenger(name, email, phone, hashedPassword, address);
        this.currentUser = passenger;
        return passenger;
    }

    public Driver registerDriver(String name, String email, String phone, String password, String confirmPassword,
                                 String licenseNumber, String vehicleModel, String plateNumber,
                                 String cabType, String color) throws Exception {
        validateCommonRegistration(name, email, phone, password, confirmPassword);

        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Driving License number is required.");
        }
        if (vehicleModel == null || vehicleModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle model is required.");
        }
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle registration number is required (e.g. KL 07 AB 1234).");
        }
        if (userRepository.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = com.errorcab.util.PasswordUtil.hash(password);
        Driver driver = userRepository.registerDriver(name, email, phone, hashedPassword, licenseNumber, vehicleModel, plateNumber, cabType, color);
        this.currentUser = driver;
        return driver;
    }

    private void validateCommonRegistration(String name, String email, String phone, String password, String confirmPassword) {
        if (name == null || name.trim().length() < 2) {
            throw new IllegalArgumentException("Please enter your full name.");
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
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

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}
