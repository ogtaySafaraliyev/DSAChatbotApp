package az.dsa.chatbot.service;

import az.dsa.chatbot.dto.SearchResult;
import az.dsa.chatbot.dto.SessionData;
import java.util.List;

public interface RecommendationService {
    
    /**
     * Get training recommendations based on session data
     * @param session User's session with collected consultation data
     * @return List of recommended trainings
     */
    List<SearchResult> getRecommendations(SessionData session);
    
    /**
     * Format recommendations into user-friendly text
     * @param recommendations List of search results
     * @param session Session data for context
     * @return Formatted response string
     */
    String formatRecommendations(List<SearchResult> recommendations, SessionData session);
}