package az.dsa.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {
    @Id
    @Column(length = 255)
    private String id;  // UUID
    
    @Column(name = "user_data", columnDefinition = "TEXT")
    private String userData;  // JSON string of collected user data
    
    @Column(name = "conversation_history", columnDefinition = "TEXT")
    private String conversationHistory;  // JSON array of messages
    
    @Column(name = "current_mode", length = 50)
    private String currentMode;  // contact, consult, query
    
    @Column(name = "current_step", length = 100)
    private String currentStep;  // Which step in the flow
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    
 // NEW: Rate limiting fields
    @Column(name = "message_count")
    private Integer messageCount = 0;
    
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;
    
    // NEW: Session metadata
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;
    
    // Helper methods
    public void incrementMessageCount() {
        this.messageCount = (this.messageCount == null ? 0 : this.messageCount) + 1;
        this.lastMessageTime = LocalDateTime.now();
    }
    
    public void resetMessageCount() {
        this.messageCount = 0;
    }
    
    public boolean isRateLimitExceeded(int maxMessages, int timeWindowMinutes) {
        if (messageCount == null || lastMessageTime == null) {
            return false;
        }
        
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(timeWindowMinutes);
        return messageCount >= maxMessages && lastMessageTime.isAfter(windowStart);
    }
}