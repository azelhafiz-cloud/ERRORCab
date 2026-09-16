package com.errorcab.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing utility using SHA-256.
 * Supports transparent verification of both hashed and legacy demo passwords.
 */
public class PasswordUtil {

    public static String hash(String password) {
        if (password == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    public static boolean verify(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) return false;
        // Direct match (for demo accounts) or hashed match
        if (inputPassword.equals(storedPassword)) return true;
        return hash(inputPassword).equalsIgnoreCase(storedPassword);
    }
}
