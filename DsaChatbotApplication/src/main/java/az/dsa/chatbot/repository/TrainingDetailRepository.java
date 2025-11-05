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
public interface TrainingDetailRepository extends JpaRepository<TrainingDetail, Long> {
    Optional<TrainingDetail> findByTrainingId(Long trainingId);
    
    @Query("SELECT td FROM TrainingDetail td WHERE " +
           "LOWER(td.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(td.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<TrainingDetail> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT td FROM TrainingDetail td WHERE " +
           "td.price BETWEEN :minPrice AND :maxPrice")
    List<TrainingDetail> findByPriceRange(@Param("minPrice") Double minPrice, 
                                          @Param("maxPrice") Double maxPrice);
}
