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
public interface GraduateRepository extends JpaRepository<Graduate, Long> {
    
    @Query("SELECT g FROM Graduate g WHERE " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.workPosition) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.workLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Graduate> searchByKeyword(@Param("keyword") String keyword);
}