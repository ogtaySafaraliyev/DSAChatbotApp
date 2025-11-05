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
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    
    @Query("SELECT t FROM Trainer t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.workLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Trainer> searchByKeyword(@Param("keyword") String keyword);
    
    Optional<Trainer> findByNameContainingIgnoreCase(String name);
}
