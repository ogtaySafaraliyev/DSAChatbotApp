package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.SessionData;
import az.dsa.chatbot.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionServiceImpl implements SessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);
    
    // In-memory storage (will migrate to Redis later)
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    
    @Value("${chatbot.session.timeout-minutes:30}")
    private int sessionTimeoutMinutes;
    
    @Value("${chatbot.session.max-messages:20}")
    private int maxMessagesPerSession;
    
    @Override
    public SessionData getOrCreateSession(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> {
            logger.info("Creating new session: {}", maskSessionId(id));
            return new SessionData(id);
        });
    }
    
    @Override
    public SessionData getSession(String sessionId) {
        SessionData session = sessions.get(sessionId);
        
        if (session != null && session.isExpired(sessionTimeoutMinutes)) {
            logger.info("Session expired: {}", maskSessionId(sessionId));
            deleteSession(sessionId);
            return null;
        }
        
        return session;
    }
    
    @Override
    public void updateSessionActivity(String sessionId) {
        SessionData session = sessions.get(sessionId);
        if (session != null) {
            session.updateActivity();
        }
    }
    
    @Override
    public void saveSession(SessionData session) {
        if (session != null && session.getSessionId() != null) {
            session.updateActivity();
            sessions.put(session.getSessionId(), session);
        }
    }
    
    @Override
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
        logger.info("Session deleted: {}", maskSessionId(sessionId));
    }
    
    @Override
    public boolean sessionExists(String sessionId) {
        SessionData session = getSession(sessionId);
        return session != null;
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    public void cleanExpiredSessions() {
        logger.info("Starting expired sessions cleanup...");
        
        int beforeCount = sessions.size();
        
        sessions.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired(sessionTimeoutMinutes);
            if (expired) {
                logger.debug("Removing expired session: {}", 
                           maskSessionId(entry.getKey()));
            }
            return expired;
        });
        
        int afterCount = sessions.size();
        int removed = beforeCount - afterCount;
        
        if (removed > 0) {
            logger.info("Cleaned {} expired sessions. Active sessions: {}", 
                       removed, afterCount);
        }
    }
    
    @Override
    public int getActiveSessionsCount() {
        return sessions.size();
    }
    
    // Helper method
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) return "****";
        return sessionId.substring(0, 4) + "****" + 
               sessionId.substring(sessionId.length() - 4);
    }
}