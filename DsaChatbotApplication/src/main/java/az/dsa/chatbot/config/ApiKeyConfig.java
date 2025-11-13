package az.dsa.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class ApiKeyConfig {
    
    @Value("${chatbot.api.keys:}")
    private String apiKeysString;
    
    @Value("${chatbot.api.authentication.enabled:false}")
    private boolean authenticationEnabled;
    
    private Set<String> validApiKeys;
    
    /**
     * Check if API key authentication is enabled
     */
    public boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }
    
    /**
     * Validate API key
     */
    public boolean isValidApiKey(String apiKey) {
        if (!authenticationEnabled) {
            return true; // Auth disabled, allow all
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        // Lazy initialization of valid keys
        if (validApiKeys == null) {
            validApiKeys = new HashSet<>();
            if (apiKeysString != null && !apiKeysString.trim().isEmpty()) {
                String[] keys = apiKeysString.split(",");
                for (String key : keys) {
                    validApiKeys.add(key.trim());
                }
            }
        }
        
        return validApiKeys.contains(apiKey.trim());
    }
    
    /**
     * Get number of configured API keys
     */
    public int getApiKeyCount() {
        if (validApiKeys == null) {
            isValidApiKey(""); // Trigger initialization
        }
        return validApiKeys != null ? validApiKeys.size() : 0;
    }
}