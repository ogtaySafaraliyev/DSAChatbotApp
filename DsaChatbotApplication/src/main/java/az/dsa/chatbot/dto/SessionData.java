package az.dsa.chatbot.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionData {
    
    private String sessionId;
    private String currentMode; // contact, consult, query
    private String currentStep; // for multi-step modes
    private Map<String, String> collectedData; // stores user inputs per step
    private List<String> conversationHistory;
    private LocalDateTime lastActivity;
    private LocalDateTime createdAt;
    
    // Constructors
    public SessionData() {
        this.collectedData = new HashMap<>();
        this.conversationHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
    }
    
    public SessionData(String sessionId) {
        this();
        this.sessionId = sessionId;
    }
    
    // Helper methods
    public void addMessage(String message) {
        this.conversationHistory.add(message);
        this.lastActivity = LocalDateTime.now();
        
        // Keep only last 20 messages
        if (this.conversationHistory.size() > 20) {
            this.conversationHistory.remove(0);
        }
    }
    
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public boolean isExpired(int timeoutMinutes) {
        return LocalDateTime.now().minusMinutes(timeoutMinutes).isAfter(lastActivity);
    }
    
    public void putData(String key, String value) {
        this.collectedData.put(key, value);
    }
    
    public String getData(String key) {
        return this.collectedData.get(key);
    }
    
    public void clearData() {
        this.collectedData.clear();
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }
    
    public String getCurrentStep() {
        return currentStep;
    }
    
    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }
    
    public Map<String, String> getCollectedData() {
        return collectedData;
    }
    
    public void setCollectedData(Map<String, String> collectedData) {
        this.collectedData = collectedData;
    }
    
    public List<String> getConversationHistory() {
        return conversationHistory;
    }
    
    public void setConversationHistory(List<String> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
    
    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}