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
    
    @Column(name = "user_data", columnDefinition = "JSONB")
    private String userData;  // JSON string of collected user data
    
    @Column(name = "conversation_history", columnDefinition = "JSONB")
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
}