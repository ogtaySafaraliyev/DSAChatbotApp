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
public interface BootcampTypeRepository extends JpaRepository<BootcampType, Long> {
    List<BootcampType> findByIsActiveTrueOrderByOrderIndex();
    List<BootcampType> findByBootcampIdAndIsActiveTrue(Long bootcampId);
}