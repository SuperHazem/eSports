package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for securely storing and retrieving user credentials
 * Used for the "Remember Password" feature
 */
public class CredentialManager {
    private static final String CREDENTIALS_FILE = System.getProperty("user.home") + File.separator + ".esports_arena_credentials";
    private static final String EMAIL_PROPERTY = "email";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String ENCRYPTION_KEY = "esports_arena_key";
    
    private static SecretKey getSecretKey() throws GeneralSecurityException {
        // In a production environment, this should use a more secure key management system
        // This is a simplified implementation for demonstration purposes
        byte[] keyBytes = ENCRYPTION_KEY.getBytes();
        // Ensure the key is 16 bytes (128 bits) for AES
        byte[] validKeyBytes = new byte[16];
        System.arraycopy(keyBytes, 0, validKeyBytes, 0, Math.min(keyBytes.length, 16));
        return new SecretKeySpec(validKeyBytes, "AES");
    }
    
    private static String encrypt(String data) throws GeneralSecurityException {
        if (data == null || data.isEmpty()) {
            return "";
        }
        
        SecretKey key = getSecretKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    private static String decrypt(String encryptedData) throws GeneralSecurityException {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return "";
        }
        
        SecretKey key = getSecretKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
    
    /**
     * Save user credentials
     * @param email User's email
     * @param password User's password
     * @return true if credentials were saved successfully
     */
    public static boolean saveCredentials(String email, String password) {
        Properties props = new Properties();
        
        try {
            // Encrypt credentials before saving
            String encryptedEmail = encrypt(email);
            String encryptedPassword = encrypt(password);
            
            props.setProperty(EMAIL_PROPERTY, encryptedEmail);
            props.setProperty(PASSWORD_PROPERTY, encryptedPassword);
            
            try (FileOutputStream out = new FileOutputStream(CREDENTIALS_FILE)) {
                props.store(out, "eSports Arena Manager Credentials");
                return true;
            }
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Load saved user credentials
     * @return String array with [email, password] or null if no credentials found
     */
    public static String[] loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            return null;
        }
        
        Properties props = new Properties();
        
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            
            String encryptedEmail = props.getProperty(EMAIL_PROPERTY);
            String encryptedPassword = props.getProperty(PASSWORD_PROPERTY);
            
            if (encryptedEmail != null && encryptedPassword != null) {
                String email = decrypt(encryptedEmail);
                String password = decrypt(encryptedPassword);
                
                return new String[] {email, password};
            }
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error loading credentials: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Clear saved credentials
     * @return true if credentials were cleared successfully
     */
    public static boolean clearCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }
}