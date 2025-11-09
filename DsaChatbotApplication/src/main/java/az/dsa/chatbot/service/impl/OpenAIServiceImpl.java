package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.service.OpenAIService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenAIServiceImpl implements OpenAIService {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAIServiceImpl.class);
    
    @Autowired(required = false)
    private com.theokanning.openai.service.OpenAiService openAiService;
    
    @Value("${openai.model:gpt-4o-mini}")
    private String model;
    
    @Value("${openai.max-tokens:500}")
    private int maxTokens;
    
    @Value("${openai.temperature:0.3}")
    private double temperature;
    
    @Value("${openai.max-retries:3}")
    private int maxRetries;
    
    @Value("${openai.retry-delay-ms:1000}")
    private long retryDelayMs;
    
    // System prompts
    private static final String SYSTEM_PROMPT = 
        "Sən DSA.az üçün rəsmi təlim chatbotusan. " +
        "Cavabları yalnız backend-dən gələn məlumatlara əsasən yaz. " +
        "Əgər cavab tapılmazsa, \"Bu məlumatı əməkdaşlarımızdan öyrənə bilərik\" de. " +
        "Dil: Azərbaycan dili.";
    
    @Override
    public String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        logger.debug("Normalizing text: {}", maskText(text));
        
        String prompt = String.format(
            "Bu yazını qrammatik və semantik baxımdan düzəlt, mənanı dəyişmə. " +
            "Yalnız düzəldilmiş mətni ver, heç bir əlavə izahat verme.\n\n" +
            "Mətn: %s", text
        );
        
        try {
            String normalized = callOpenAI(prompt, 100, 0.1);
            logger.debug("Normalized result: {}", maskText(normalized));
            return normalized != null ? normalized.trim() : text;
            
        } catch (Exception e) {
            logger.error("Error normalizing text: {}", e.getMessage());
            return text; // Return original if normalization fails
        }
    }
    
    @Override
    public String detectIntent(String normalizedText) {
        if (normalizedText == null || normalizedText.trim().isEmpty()) {
            return "unclear";
        }
        
        logger.debug("Detecting intent for: {}", maskText(normalizedText));
        
        String prompt = String.format(
            "Aşağıdakı mesajın məqsədini seç. Yalnız bir söz cavab ver.\n\n" +
            "Seçimlər:\n" +
            "- contact: İstifadəçi əlaqə saxlamaq, zəng etmək, müraciət etmək istəyir\n" +
            "- consult: İstifadəçi təlim öyrənmək, kurs seçmək, məsləhət almaq istəyir\n" +
            "- query: İstifadəçinin konkret sualı var (qiymət, müddət, tələb və s.)\n" +
            "- trainer: İstifadəçi təlimçilər haqqında soruşur\n" +
            "- greeting: İstifadəçi salam verir, salamlaşır\n" +
            "- unclear: Məqsəd aydın deyil\n\n" +
            "Mesaj: %s\n\n" +
            "Cavab (yalnız bir söz):", normalizedText
        );
        
        try {
            String intent = callOpenAI(prompt, 20, 0.2);
            intent = intent != null ? intent.trim().toLowerCase() : "unclear";
            
            // Validate intent
            List<String> validIntents = List.of(
                "contact", "consult", "query", "trainer", "greeting", "unclear"
            );
            
            if (!validIntents.contains(intent)) {
                logger.warn("Invalid intent detected: {}, defaulting to unclear", intent);
                intent = "unclear";
            }
            
            logger.debug("Detected intent: {}", intent);
            return intent;
            
        } catch (Exception e) {
            logger.error("Error detecting intent: {}", e.getMessage());
            return "unclear";
        }
    }
    
    @Override
    public String formatResponse(String rawData, String userQuestion) {
        if (rawData == null || rawData.trim().isEmpty()) {
            return "Üzr istəyirik, məlumat tapılmadı.";
        }
        
        logger.debug("Formatting response for question: {}", maskText(userQuestion));
        
        String prompt = String.format(
            "%s\n\n" +
            "Bu məlumat əsasında istifadəçiyə təbii və aydın cavab yaz. " +
            "Cavab qısa və konkret olsun.\n\n" +
            "İstifadəçinin sualı: %s\n\n" +
            "Məlumat: %s\n\n" +
            "Cavab:",
            SYSTEM_PROMPT, userQuestion, rawData
        );
        
        try {
            String response = callOpenAI(prompt, maxTokens, temperature);
            return response != null ? response.trim() : rawData;
            
        } catch (Exception e) {
            logger.error("Error formatting response: {}", e.getMessage());
            return rawData; // Return raw data if formatting fails
        }
    }
    
    @Override
    public boolean isAmbiguous(String text) {
        if (text == null || text.trim().isEmpty()) {
            return true;
        }
        
        // Check if text is too short
        if (text.trim().split("\\s+").length < 2) {
            return true;
        }
        
        // Check if text is too vague
        List<String> vaguePatterns = List.of(
            "məlumat", "haqqında", "bilmək", "nədir", "necədir"
        );
        
        String lower = text.toLowerCase();
        long vagueCount = vaguePatterns.stream()
                .filter(lower::contains)
                .count();
        
        // If contains 2+ vague words without specifics, it's ambiguous
        if (vagueCount >= 2) {
            boolean hasSpecifics = lower.contains("python") || 
                                   lower.contains("sql") ||
                                   lower.contains("machine learning") ||
                                   lower.contains("data");
            
            return !hasSpecifics;
        }
        
        return false;
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Call OpenAI API with retry logic
     */
    private String callOpenAI(String prompt, int maxTokens, double temperature) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_PROMPT));
                messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
                
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model(model)
                        .messages(messages)
                        .maxTokens(maxTokens)
                        .temperature(temperature)
                        .build();
                
                var response = openAiService.createChatCompletion(request);
                
                if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                    String content = response.getChoices().get(0).getMessage().getContent();
                    logger.debug("OpenAI API call successful (attempt {})", attempt + 1);
                    return content;
                }
                
                logger.warn("OpenAI returned empty response (attempt {})", attempt + 1);
                
            } catch (Exception e) {
                lastException = e;
                attempt++;
                logger.warn("OpenAI API call failed (attempt {}): {}", 
                           attempt, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        logger.error("OpenAI API call failed after {} attempts", maxRetries);
        if (lastException != null) {
            throw new RuntimeException("OpenAI API error", lastException);
        }
        
        return null;
    }
    
    private String maskText(String text) {
        if (text == null || text.length() <= 20) return text;
        return text.substring(0, 20) + "...";
    }
}