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
public interface LeadRepository extends JpaRepository<Lead, Long> {
    
    List<Lead> findByPhoneContaining(String phone);
    
    @Query("SELECT l FROM Lead l WHERE l.createdAt >= :startDate ORDER BY l.createdAt DESC")
    List<Lead> findRecentLeads(@Param("startDate") LocalDateTime startDate);
    
    // Check if phone exists (to avoid duplicates)
    boolean existsByPhone(String phone);
}