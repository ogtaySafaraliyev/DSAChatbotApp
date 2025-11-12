package az.dsa.chatbot.service;

import az.dsa.chatbot.entity.Trainer;
import java.util.List;

public interface TrainerService {
    
    /**
     * Search trainers by keyword
     */
    List<Trainer> searchTrainers(String keyword);
    
    /**
     * Get all trainers
     */
    List<Trainer> getAllTrainers();
    
    /**
     * Get trainer by ID
     */
    Trainer getTrainerById(Long id);
    
    /**
     * Format trainer information for chat response
     */
    String formatTrainerInfo(List<Trainer> trainers);
    
    /**
     * Format single trainer details
     */
    String formatSingleTrainer(Trainer trainer);
}