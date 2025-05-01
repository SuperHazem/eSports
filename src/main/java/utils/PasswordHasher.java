package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher {

    // Hash a password using SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback to plain text if hashing fails
        }
    }

    // Verify a password against a stored hash
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            // Hash the input password using the same algorithm
            String hashedInput = hashPassword(plainPassword);

            // Compare the hashed input with the stored hash
            return hashedInput.equals(storedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}