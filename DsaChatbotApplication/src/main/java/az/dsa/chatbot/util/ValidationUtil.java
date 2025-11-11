package az.dsa.chatbot.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {
    
    // Phone pattern for Azerbaijan: +994XXXXXXXXX
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+994[0-9]{9}$");
    
    // Email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    /**
     * Validate phone number format
     * @param phone Phone number to validate
     * @return true if valid
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate email format
     * @param email Email to validate
     * @return true if valid
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate name (minimum 3 characters, only letters and spaces)
     * @param name Name to validate
     * @return true if valid
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = name.trim();
        
        // Minimum length
        if (trimmed.length() < 3) {
            return false;
        }
        
        // Check if contains at least one letter
        return trimmed.matches(".*[a-zA-ZəöüğıçşƏÖÜĞIÇŞ].*");
    }
    
    /**
     * Normalize phone number (remove spaces, dashes, etc.)
     * @param phone Phone number to normalize
     * @return Normalized phone
     */
    public String normalizePhone(String phone) {
        if (phone == null) return null;
        
        // Remove all non-digit characters except +
        String normalized = phone.replaceAll("[^0-9+]", "");
        
        // If starts with 994 without +, add it
        if (normalized.matches("^994[0-9]{9}$")) {
            normalized = "+" + normalized;
        }
        
        // If starts with 0, replace with +994
        if (normalized.matches("^0[0-9]{9}$")) {
            normalized = "+994" + normalized.substring(1);
        }
        
        return normalized;
    }
    
    /**
     * Validate message length
     * @param message Message to validate
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if valid
     */
    public boolean isValidMessage(String message, int minLength, int maxLength) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        int length = message.trim().length();
        return length >= minLength && length <= maxLength;
    }
}