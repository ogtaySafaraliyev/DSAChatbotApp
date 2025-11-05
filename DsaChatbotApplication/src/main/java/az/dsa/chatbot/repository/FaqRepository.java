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
public interface FaqRepository extends JpaRepository<Faq, Long> {
    
    @Query("SELECT f FROM Faq f WHERE " +
           "LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Faq> searchByKeyword(@Param("keyword") String keyword);
    
    // Full-text search using PostgreSQL
    @Query(value = "SELECT * FROM faq WHERE " +
           "to_tsvector('simple', question || ' ' || answer) @@ plainto_tsquery('simple', :query) " +
           "ORDER BY ts_rank(to_tsvector('simple', question || ' ' || answer), plainto_tsquery('simple', :query)) DESC " +
           "LIMIT :limit",
           nativeQuery = true)
    List<Faq> fullTextSearch(@Param("query") String query, @Param("limit") int limit);
    
    @Query("SELECT f FROM Faq f WHERE " +
           "LOWER(f.question) LIKE LOWER(CONCAT('%', :q1, '%')) OR " +
           "LOWER(f.question) LIKE LOWER(CONCAT('%', :q2, '%')) OR " +
           "LOWER(f.answer) LIKE LOWER(CONCAT('%', :q1, '%')) OR " +
           "LOWER(f.answer) LIKE LOWER(CONCAT('%', :q2, '%'))")
    List<Faq> findRelevantFaqs(@Param("q1") String keyword1, @Param("q2") String keyword2);
}