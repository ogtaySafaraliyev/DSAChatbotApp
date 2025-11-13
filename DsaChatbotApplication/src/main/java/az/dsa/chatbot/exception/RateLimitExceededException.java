package az.dsa.chatbot.exception;

public class RateLimitExceededException extends RuntimeException {
    
    private String sessionId;
    private int messageCount;
    private int maxMessages;
    
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    public RateLimitExceededException(String sessionId, int messageCount, int maxMessages) {
        super(String.format(
            "Rate limit exceeded for session %s. Sent %d messages (max: %d)",
            maskSessionId(sessionId), messageCount, maxMessages
        ));
        this.sessionId = sessionId;
        this.messageCount = messageCount;
        this.maxMessages = maxMessages;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public int getMessageCount() {
        return messageCount;
    }
    
    public int getMaxMessages() {
        return maxMessages;
    }
    
    private static String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) return "****";
        return sessionId.substring(0, 4) + "****" + 
               sessionId.substring(sessionId.length() - 4);
    }
}