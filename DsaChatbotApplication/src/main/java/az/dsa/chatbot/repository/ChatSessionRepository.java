package az.dsa.chatbot.repository;

import az.dsa.chatbot.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {
    
    // Find expired sessions for cleanup
    @Query("SELECT cs FROM ChatSession cs WHERE cs.expiresAt < :now")
    List<ChatSession> findExpiredSessions(@Param("now") LocalDateTime now);
    
    // Find inactive sessions
    @Query("SELECT cs FROM ChatSession cs WHERE cs.lastActivity < :cutoffTime")
    List<ChatSession> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Delete expired sessions
    void deleteByExpiresAtBefore(LocalDateTime expiryTime);
}