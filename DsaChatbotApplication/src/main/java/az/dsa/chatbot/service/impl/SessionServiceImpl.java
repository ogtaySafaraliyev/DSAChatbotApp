package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.SessionData;
import az.dsa.chatbot.entity.ChatSession;
import az.dsa.chatbot.repository.ChatSessionRepository;
import az.dsa.chatbot.service.SessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SessionServiceImpl implements SessionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionServiceImpl.class);
    
    @Autowired
    private ChatSessionRepository chatSessionRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${chatbot.session.timeout-minutes:30}")
    private int sessionTimeoutMinutes;
    
    @Value("${chatbot.session.max-messages:20}")
    private int maxMessagesPerSession;
    
    @Override
    @Transactional
    public SessionData getOrCreateSession(String sessionId) {
        Optional<ChatSession> existingSession = chatSessionRepository.findById(sessionId);
        
        if (existingSession.isPresent()) {
            ChatSession session = existingSession.get();
            
            // Check if expired
            if (session.getExpiresAt() != null && 
                LocalDateTime.now().isAfter(session.getExpiresAt())) {
                logger.info("Session expired, creating new: {}", maskSessionId(sessionId));
                chatSessionRepository.delete(session);
                return createNewSession(sessionId);
            }
            
            // Check if blocked
            if (Boolean.TRUE.equals(session.getIsBlocked())) {
                logger.warn("Blocked session attempted to connect: {}", maskSessionId(sessionId));
                // Still return session but controller should check this
            }
            
            logger.debug("Retrieved existing session: {}", maskSessionId(sessionId));
            return convertToSessionData(session);
        }
        
        logger.info("Creating new session: {}", maskSessionId(sessionId));
        return createNewSession(sessionId);
    }
    
    @Override
    public SessionData getSession(String sessionId) {
        Optional<ChatSession> session = chatSessionRepository.findById(sessionId);
        
        if (session.isEmpty()) {
            return null;
        }
        
        ChatSession chatSession = session.get();
        
        // Check expiration
        if (chatSession.getExpiresAt() != null && 
            LocalDateTime.now().isAfter(chatSession.getExpiresAt())) {
            logger.debug("Session expired: {}", maskSessionId(sessionId));
            return null;
        }
        
        return convertToSessionData(chatSession);
    }
    
    @Override
    @Transactional
    public void updateSessionActivity(String sessionId) {
        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
        
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setLastActivity(LocalDateTime.now());
            session.setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
            session.incrementMessageCount();
            
            chatSessionRepository.save(session);
            logger.debug("Updated session activity: {}", maskSessionId(sessionId));
        }
    }
    
    @Override
    @Transactional
    public void saveSession(SessionData sessionData) {
        if (sessionData == null || sessionData.getSessionId() == null) {
            logger.warn("Attempted to save null session");
            return;
        }
        
        try {
            ChatSession chatSession = convertToChatSession(sessionData);
            chatSession.setLastActivity(LocalDateTime.now());
            chatSession.setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
            
            chatSessionRepository.save(chatSession);
            logger.debug("Session saved: {}", maskSessionId(sessionData.getSessionId()));
            
        } catch (Exception e) {
            logger.error("Error saving session: {}", e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        chatSessionRepository.deleteById(sessionId);
        logger.info("Session deleted: {}", maskSessionId(sessionId));
    }
    
    @Override
    public boolean sessionExists(String sessionId) {
        return chatSessionRepository.existsById(sessionId);
    }
    
    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    public void cleanExpiredSessions() {
        logger.info("Starting expired sessions cleanup...");
        
        try {
            List<ChatSession> expiredSessions = 
                chatSessionRepository.findExpiredSessions(LocalDateTime.now());
            
            int count = expiredSessions.size();
            
            if (count > 0) {
                chatSessionRepository.deleteAll(expiredSessions);
                logger.info("Cleaned {} expired sessions", count);
            } else {
                logger.debug("No expired sessions to clean");
            }
            
        } catch (Exception e) {
            logger.error("Error cleaning expired sessions: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public int getActiveSessionsCount() {
        return (int) chatSessionRepository.count();
    }
    
    // ===== NEW: Rate Limiting Methods =====
    
    public boolean isRateLimitExceeded(String sessionId) {
        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
        
        if (sessionOpt.isEmpty()) {
            return false;
        }
        
        ChatSession session = sessionOpt.get();
        return session.isRateLimitExceeded(maxMessagesPerSession, 60); // 60 minutes window
    }
    
    @Transactional
    public void blockSession(String sessionId, String reason) {
        Optional<ChatSession> sessionOpt = chatSessionRepository.findById(sessionId);
        
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setIsBlocked(true);
            chatSessionRepository.save(session);
            
            logger.warn("Session blocked: {} - Reason: {}", maskSessionId(sessionId), reason);
        }
    }
    
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Every hour at minute 0
    public void resetRateLimitCounters() {
        logger.debug("Resetting rate limit counters for active sessions");
        
        try {
            List<ChatSession> activeSessions = chatSessionRepository.findAll();
            
            for (ChatSession session : activeSessions) {
                if (session.getLastMessageTime() != null) {
                    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                    
                    if (session.getLastMessageTime().isBefore(oneHourAgo)) {
                        session.resetMessageCount();
                    }
                }
            }
            
            chatSessionRepository.saveAll(activeSessions);
            logger.debug("Rate limit counters reset");
            
        } catch (Exception e) {
            logger.error("Error resetting rate limit counters: {}", e.getMessage());
        }
    }
    
    // ===== CONVERSION METHODS =====
    
    private SessionData createNewSession(String sessionId) {
        SessionData sessionData = new SessionData(sessionId);
        sessionData.setCreatedAt(LocalDateTime.now());
        sessionData.setLastActivity(LocalDateTime.now());
        
        // Save to database immediately
        saveSession(sessionData);
        
        return sessionData;
    }
    
    private SessionData convertToSessionData(ChatSession chatSession) {
        SessionData sessionData = new SessionData(chatSession.getId());
        sessionData.setCurrentMode(chatSession.getCurrentMode());
        sessionData.setCurrentStep(chatSession.getCurrentStep());
        sessionData.setCreatedAt(chatSession.getCreatedAt());
        sessionData.setLastActivity(chatSession.getLastActivity());
        
        // Deserialize collected data
        if (chatSession.getUserData() != null && !chatSession.getUserData().isEmpty()) {
            try {
                Map<String, String> userData = objectMapper.readValue(
                    chatSession.getUserData(), 
                    new TypeReference<Map<String, String>>() {}
                );
                sessionData.setCollectedData(userData);
            } catch (JsonProcessingException e) {
                logger.error("Error deserializing user data: {}", e.getMessage());
            }
        }
        
        // Deserialize conversation history
        if (chatSession.getConversationHistory() != null && 
            !chatSession.getConversationHistory().isEmpty()) {
            try {
                List<String> history = objectMapper.readValue(
                    chatSession.getConversationHistory(), 
                    new TypeReference<List<String>>() {}
                );
                sessionData.setConversationHistory(history);
            } catch (JsonProcessingException e) {
                logger.error("Error deserializing conversation history: {}", e.getMessage());
            }
        }
        
        return sessionData;
    }
    
    private ChatSession convertToChatSession(SessionData sessionData) {
        ChatSession chatSession = chatSessionRepository.findById(sessionData.getSessionId())
            .orElse(new ChatSession());
        
        chatSession.setId(sessionData.getSessionId());
        chatSession.setCurrentMode(sessionData.getCurrentMode());
        chatSession.setCurrentStep(sessionData.getCurrentStep());
        chatSession.setCreatedAt(sessionData.getCreatedAt());
        chatSession.setLastActivity(sessionData.getLastActivity());
        
        // Serialize collected data
        try {
            String userDataJson = objectMapper.writeValueAsString(
                sessionData.getCollectedData()
            );
            chatSession.setUserData(userDataJson);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing user data: {}", e.getMessage());
        }
        
        // Serialize conversation history
        try {
            String historyJson = objectMapper.writeValueAsString(
                sessionData.getConversationHistory()
            );
            chatSession.setConversationHistory(historyJson);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing conversation history: {}", e.getMessage());
        }
        
        // Set expiration
        if (chatSession.getExpiresAt() == null) {
            chatSession.setExpiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes));
        }
        
        return chatSession;
    }
    
    // Helper method
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) return "****";
        return sessionId.substring(0, 4) + "****" + 
               sessionId.substring(sessionId.length() - 4);
    }
}