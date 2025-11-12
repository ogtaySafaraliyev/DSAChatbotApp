package az.dsa.chatbot.service;

import az.dsa.chatbot.entity.Graduate;
import java.util.List;

public interface GraduateService {
    
    /**
     * Search graduates by keyword
     */
    List<Graduate> searchGraduates(String keyword);
    
    /**
     * Get all graduates
     */
    List<Graduate> getAllGraduates();
    
    /**
     * Format graduate success stories for chat response
     */
    String formatGraduateInfo(List<Graduate> graduates);
    
    /**
     * Get random success stories (for inspiration)
     */
    List<Graduate> getRandomSuccessStories(int count);
}