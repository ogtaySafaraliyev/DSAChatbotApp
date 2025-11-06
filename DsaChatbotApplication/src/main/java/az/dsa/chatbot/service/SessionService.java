package az.dsa.chatbot.service;

import az.dsa.chatbot.dto.SessionData;

public interface SessionService {
    
    /**
     * Get or create session
     */
    SessionData getOrCreateSession(String sessionId);
    
    /**
     * Get existing session (returns null if not exists)
     */
    SessionData getSession(String sessionId);
    
    /**
     * Update session activity timestamp
     */
    void updateSessionActivity(String sessionId);
    
    /**
     * Save session data
     */
    void saveSession(SessionData session);
    
    /**
     * Delete session
     */
    void deleteSession(String sessionId);
    
    /**
     * Check if session exists
     */
    boolean sessionExists(String sessionId);
    
    /**
     * Clean expired sessions (scheduled task)
     */
    void cleanExpiredSessions();
    
    /**
     * Get total active sessions count
     */
    int getActiveSessionsCount();
}