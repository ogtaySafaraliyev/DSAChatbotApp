package az.dsa.chatbot.service;

public interface OpenAIService {
    
    /**
     * Normalize text (fix spelling, grammar)
     * Input: "piton kursu ne qdr ckr?"
     * Output: "Python kursu nə qədər çəkir?"
     */
    String normalizeText(String text);
    
    /**
     * Detect intent from message
     * Returns: contact, consult, query, trainer, greeting, unclear
     */
    String detectIntent(String normalizedText);
    
    /**
     * Format response naturally
     * Input: Raw data from DB
     * Output: Natural Azerbaijani response
     */
    String formatResponse(String rawData, String userQuestion);
    
    /**
     * Check if question is ambiguous
     */
    boolean isAmbiguous(String text);
}