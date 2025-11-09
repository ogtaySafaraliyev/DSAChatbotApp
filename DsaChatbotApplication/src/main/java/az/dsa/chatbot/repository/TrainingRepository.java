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
	List<Training> findByIsActiveTrueOrderByOrderIndex();

	List<Training> findByBootcampTypeIdAndIsActiveTrue(Long bootcampTypeId);

//	@Query("SELECT t FROM Training t WHERE t.isActive = true AND "
//			+ "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
//	List<Training> searchByKeyword(@Param("keyword") String keyword);

	Optional<Training> findByIdAndIsActiveTrue(Long id);

	/**
	 * Search trainings by keyword in title
	 */
	@Query("SELECT t FROM Training t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Training> searchByKeyword(@Param("keyword") String keyword);

	/**
	 * Find active trainings
	 */
	@Query("SELECT t FROM Training t WHERE t.isActive = true")
	List<Training> findAllActive();

	/**
	 * Find by bootcamp type ID
	 */
	@Query("SELECT t FROM Training t WHERE t.bootcampTipiId = :bootcampTypeId AND t.isActive = true")
	List<Training> findByBootcampType(@Param("bootcampTypeId") Long bootcampTypeId);
}
