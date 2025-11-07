package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.model.Intent;
import az.dsa.chatbot.service.IntentService;
import az.dsa.chatbot.service.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class IntentServiceImpl implements IntentService {
    
    private static final Logger logger = LoggerFactory.getLogger(IntentServiceImpl.class);
    
    @Autowired
    private OpenAIService openAIService;
    
    @Override
    public Intent determineIntent(String normalizedText) {
        if (normalizedText == null || normalizedText.trim().isEmpty()) {
            return Intent.UNCLEAR;
        }
        
        String lower = normalizedText.toLowerCase();
        
        // First: Try keyword matching (faster)
        Intent keywordIntent = matchByKeywords(lower);
        if (keywordIntent != Intent.UNCLEAR) {
            logger.debug("Intent determined by keywords: {}", keywordIntent);
            return keywordIntent;
        }
        
        // Second: Use AI (more accurate but slower)
        try {
            String aiIntent = openAIService.detectIntent(normalizedText);
            Intent intent = Intent.fromString(aiIntent);
            logger.debug("Intent determined by AI: {}", intent);
            return intent;
            
        } catch (Exception e) {
            logger.error("Error in AI intent detection: {}", e.getMessage());
            return Intent.UNCLEAR;
        }
    }
    
    @Override
    public boolean matchesKeywords(String text, Intent intent) {
        if (text == null) return false;
        
        String lower = text.toLowerCase();
        
        switch (intent) {
            case GREETING:
                return getGreetingKeywords().stream().anyMatch(lower::contains);
                
            case CONTACT:
                return getContactKeywords().stream().anyMatch(lower::contains);
                
            case CONSULT:
                return getConsultKeywords().stream().anyMatch(lower::contains);
                
            case QUERY:
                return getQueryKeywords().stream().anyMatch(lower::contains);
                
            case TRAINER:
                return getTrainerKeywords().stream().anyMatch(lower::contains);
                
            default:
                return false;
        }
    }
    
    // ===== PRIVATE HELPERS =====
    
    private Intent matchByKeywords(String text) {
        // Check in priority order
        
        if (getGreetingKeywords().stream().anyMatch(text::contains)) {
            return Intent.GREETING;
        }
        
        if (getContactKeywords().stream().anyMatch(text::contains)) {
            return Intent.CONTACT;
        }
        
        if (getConsultKeywords().stream().anyMatch(text::contains)) {
            return Intent.CONSULT;
        }
        
        if (getTrainerKeywords().stream().anyMatch(text::contains)) {
            return Intent.TRAINER;
        }
        
        if (getQueryKeywords().stream().anyMatch(text::contains)) {
            return Intent.QUERY;
        }
        
        return Intent.UNCLEAR;
    }
    
    private List<String> getGreetingKeywords() {
        return Arrays.asList(
            "salam", "salamlar", "sabah", "sabahınız xeyir",
            "axşamınız xeyir", "gün aydın", "hello", "hi", "hey"
        );
    }
    
    private List<String> getContactKeywords() {
        return Arrays.asList(
            "əlaqə", "zəng", "telefon", "contact",
            "müraciət", "əməkdaş", "yazın", "email"
        );
    }
    
    private List<String> getConsultKeywords() {
        return Arrays.asList(
            "öyrənmək istəyirəm", "təlim seç", "kurs seç",
            "öyrənmək", "məsləhət", "konsultasiya", "tövsiyə",
            "başlamaq istəyirəm"
        );
    }
    
    private List<String> getQueryKeywords() {
        return Arrays.asList(
            "nə qədər", "qiymət", "haqqında", "müddət",
            "nə vaxt", "tələb", "sertifikat", "bootcamp"
        );
    }
    
    private List<String> getTrainerKeywords() {
        return Arrays.asList(
            "təlimçi", "müəllim", "trainer", "kim tədris edir",
            "kim öyrədir"
        );
    }
}