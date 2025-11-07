package az.dsa.chatbot.service;

import az.dsa.chatbot.model.Intent;

public interface IntentService {
    
    /**
     * Determine intent using both keywords and AI
     */
    Intent determineIntent(String normalizedText);
    
    /**
     * Check if keywords match intent
     */
    boolean matchesKeywords(String text, Intent intent);
}