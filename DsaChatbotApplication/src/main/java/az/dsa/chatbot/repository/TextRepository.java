package az.dsa.chatbot.repository;

import az.dsa.chatbot.entity.Text;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextRepository extends JpaRepository<Text, Long> {
    
    /**
     * Search in title, description, and information fields
     */
    @Query("SELECT t FROM Text t WHERE " +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.information) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Text> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find by trainings ID
     */
    List<Text> findByTrainingsId(Integer trainingsId);
    
    /**
     * Search in title only
     */
    @Query("SELECT t FROM Text t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Text> searchByTitle(@Param("keyword") String keyword);
}