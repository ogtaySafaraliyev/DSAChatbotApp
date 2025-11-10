package az.dsa.chatbot.util;

import az.dsa.chatbot.entity.Text;
import az.dsa.chatbot.entity.Training;
import az.dsa.chatbot.repository.TextRepository;
import az.dsa.chatbot.repository.TrainingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TrainingTextMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(TrainingTextMapper.class);
    
    @Autowired
    private TrainingRepository trainingRepository;
    
    @Autowired
    private TextRepository textRepository;
    
    // Cache for performance
    private Map<Long, Text> trainingIdToTextCache = new HashMap<>();
    private boolean cacheInitialized = false;
    
    /**
     * Get Text details for a Training
     */
    public Text getTextForTraining(Training training) {
        if (training == null) return null;
        
        initializeCacheIfNeeded();
        
        return trainingIdToTextCache.get(training.getId());
    }
    
    /**
     * Get Text details by training ID
     */
    public Text getTextForTrainingId(Long trainingId) {
        if (trainingId == null) return null;
        
        initializeCacheIfNeeded();
        
        return trainingIdToTextCache.get(trainingId);
    }
    
    /**
     * Get Training for a Text
     */
    public Training getTrainingForText(Text text) {
        if (text == null || text.getTrainingsId() == null) return null;
        
        return trainingRepository.findById(text.getTrainingsId().longValue()).orElse(null);
    }
    
    /**
     * Clear cache (useful for updates)
     */
    public void clearCache() {
        trainingIdToTextCache.clear();
        cacheInitialized = false;
        logger.info("Training-Text cache cleared");
    }
    
    /**
     * Initialize cache on first use
     */
    private void initializeCacheIfNeeded() {
        if (cacheInitialized) return;
        
        logger.info("Initializing Training-Text cache...");
        
        try {
            List<Text> allTexts = textRepository.findAll();
            
            for (Text text : allTexts) {
                if (text.getTrainingsId() != null) {
                    trainingIdToTextCache.put(text.getTrainingsId().longValue(), text);
                }
            }
            
            cacheInitialized = true;
            logger.info("Cache initialized with {} mappings", trainingIdToTextCache.size());
            
        } catch (Exception e) {
            logger.error("Error initializing cache: {}", e.getMessage());
        }
    }
}