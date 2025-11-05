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
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    
    List<TrainingSession> findByTrainingDetailId(Long trainingDetailId);
    
    @Query("SELECT ts FROM TrainingSession ts WHERE " +
           "ts.sessionDate >= :startDate ORDER BY ts.sessionDate, ts.sessionHour")
    List<TrainingSession> findUpcomingSessions(@Param("startDate") java.time.LocalDate startDate);
    
    @Query("SELECT ts FROM TrainingSession ts " +
           "WHERE ts.trainingDetail.id = :trainingDetailId " +
           "ORDER BY ts.sessionNumber")
    List<TrainingSession> findByTrainingDetailIdOrderBySessionNumber(@Param("trainingDetailId") Long trainingDetailId);
}
