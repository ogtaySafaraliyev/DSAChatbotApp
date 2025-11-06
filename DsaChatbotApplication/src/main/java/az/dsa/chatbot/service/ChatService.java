package az.dsa.chatbot.service;

import az.dsa.chatbot.dto.ChatRequest;
import az.dsa.chatbot.dto.ChatResponse;

public interface ChatService {
    
    /**
     * Process incoming chat message
     */
    ChatResponse processMessage(ChatRequest request);
    
    /**
     * Reset user session
     */
    void resetSession(String sessionId);
    
    /**
     * Clean expired sessions
     */
    void cleanExpiredSessions();
}