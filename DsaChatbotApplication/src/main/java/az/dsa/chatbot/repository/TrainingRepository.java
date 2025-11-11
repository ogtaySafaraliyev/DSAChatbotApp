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
public interface TrainingRepository extends JpaRepository<Training, Long> {
	 /**
     * Find all active trainings ordered by index
     */
    List<Training> findByIsActiveTrueOrderByOrderIndex();
    
    /**
     * Find trainings by bootcamp type ID (FIXED)
     */
    @Query("SELECT t FROM Training t WHERE t.bootcampType.id = :bootcampTypeId AND t.isActive = true")
    List<Training> findByBootcampTypeIdAndIsActiveTrue(@Param("bootcampTypeId") Long bootcampTypeId);


//	@Query("SELECT t FROM Training t WHERE t.isActive = true AND "
//			+ "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//	List<Training> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * Find by ID and active status
     */
    Optional<Training> findByIdAndIsActiveTrue(Long id);

    /**
     * Search trainings by keyword in title
     */
    @Query("SELECT t FROM Training t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Training> searchByKeyword(@Param("keyword") String keyword);
    
    
    /**
     * Find all active trainings
     */
    @Query("SELECT t FROM Training t WHERE t.isActive = true ORDER BY t.orderIndex")
    List<Training> findAllActive();
    
    /**
     * Find by bootcamp type with details (FIXED - using JOIN FETCH for performance)
     */
    @Query("SELECT t FROM Training t " +
           "LEFT JOIN FETCH t.bootcampType bt " +
           "WHERE bt.id = :bootcampTypeId AND t.isActive = true " +
           "ORDER BY t.orderIndex")
    List<Training> findByBootcampTypeWithDetails(@Param("bootcampTypeId") Long bootcampTypeId);
	
    /**
     * Count active trainings by bootcamp type
     */
    @Query("SELECT COUNT(t) FROM Training t WHERE t.bootcampType.id = :bootcampTypeId AND t.isActive = true")
    long countByBootcampTypeId(@Param("bootcampTypeId") Long bootcampTypeId);

    
    
    // extra method unused
//	/**
//	 * Find by bootcamp type ID
//	 */
//	@Query("SELECT t FROM Training t WHERE t.bootcampTipiId = :bootcampTypeId AND t.isActive = true")
//	List<Training> findByBootcampType(@Param("bootcampTypeId") Long bootcampTypeId);
}
