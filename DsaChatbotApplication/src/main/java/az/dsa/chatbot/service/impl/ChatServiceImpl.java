package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.ChatRequest;
import az.dsa.chatbot.dto.ChatResponse;
import az.dsa.chatbot.dto.SessionData;
import az.dsa.chatbot.model.Intent;
import az.dsa.chatbot.model.Mode;
import az.dsa.chatbot.service.ChatService;
import az.dsa.chatbot.service.IntentService;
import az.dsa.chatbot.service.OpenAIService;
import az.dsa.chatbot.service.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private IntentService intentService;
    
    // TODO: Will inject these in next steps
    // @Autowired
    // private OpenAIService openAIService;
    // @Autowired
    // private SearchService searchService;
    // @Autowired
    // private IntentService intentService;
    
    @Override
    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId();
        String message = request.getMessage().trim();
        
        logger.info("Processing message for session: {}", maskSessionId(sessionId));
        
        // Get or create session
        SessionData session = sessionService.getOrCreateSession(sessionId);
        
        // Add message to history
        session.addMessage("User: " + message);
        
        // Check if empty message
        if (message.isEmpty()) {
            return createResponse(session, 
                "Zəhmət olmasa məqsədinizi daha aydın yazın");
        }
        
        // Check current mode
        Mode currentMode = Mode.fromString(session.getCurrentMode());
        
        // Route based on mode
        ChatResponse response;
        switch (currentMode) {
            case CONTACT:
                response = handleContactMode(session, message);
                break;
            case CONSULT:
                response = handleConsultMode(session, message);
                break;
            default:
                response = handleInitialMessage(session, message);
                break;
        }
        
        // Add bot response to history
        session.addMessage("Bot: " + response.getReply());
        
        // Save session
        sessionService.saveSession(session);
        
        return response;
    }
    
    @Override
    public void resetSession(String sessionId) {
        logger.info("Resetting session: {}", maskSessionId(sessionId));
        sessionService.deleteSession(sessionId);
    }
    
    @Override
    public void cleanExpiredSessions() {
        sessionService.cleanExpiredSessions();
    }
    
    // ===== MODE HANDLERS (Skeleton) =====
    
//    private ChatResponse handleInitialMessage(SessionData session, String message) {
//        // TODO: Step 1.3 - Implement OpenAI normalization + intent detection
//        // TODO: Step 2.x - Implement database search
//        
//        // For now: Simple keyword detection
//        String lowerMessage = message.toLowerCase();
//        
//        // Greeting detection
//        if (isGreeting(lowerMessage)) {
//            return createResponse(session,
//                "Salam! Data Science Academy-ə xoş gəlmisiniz! " +
//                "Sizə necə kömək edə bilərəm?\n\n" +
//                "• Təlimlərimiz haqqında məlumat\n" +
//                "• Qeydiyyat\n" +
//                "• Əlaqə");
//        }
//        
//        // Contact intent detection
//        if (isContactIntent(lowerMessage)) {
//            session.setCurrentMode(Mode.CONTACT.getValue());
//            session.setCurrentStep("awaiting_name");
//            return createResponse(session,
//                "Əlaqə üçün zəhmət olmasa ad və soyadınızı yazın");
//        }
//        
//        // Consult intent detection
//        if (isConsultIntent(lowerMessage)) {
//            session.setCurrentMode(Mode.CONSULT.getValue());
//            session.setCurrentStep("awaiting_experience");
//            return createResponse(session,
//                "Sizə uyğun təlimi seçməyə kömək edim.\n" +
//                "Hansı sahədə təcrübəniz var? " +
//                "(Məsələn: proqramlaşdırma, analitika, və ya yoxdur)");
//        }
//        
//        // Training/course query detection
//        if (isTrainingQuery(lowerMessage)) {
//            // TODO: Step 2.x - Search in database
//            return createResponse(session,
//                "Təlimlərimiz haqqında məlumat axtarıram...\n" +
//                "(Database search will be implemented in Step 2.x)");
//        }
//        
//        // Unclear intent
//        return createResponse(session,
//            "Hansı sahədə sizə kömək edə bilərəm?\n\n" +
//            "• Təlimlər haqqında məlumat\n" +
//            "• Qeydiyyat və əlaqə\n" +
//            "• Konsultasiya");
//    }
    
    private ChatResponse handleInitialMessage(SessionData session, String message) {
        
        // Step 1: Normalize text
        String normalizedText = openAIService.normalizeText(message);
        logger.debug("Normalized: {} -> {}", message, normalizedText);
        
        // Step 2: Check if ambiguous
        if (openAIService.isAmbiguous(normalizedText)) {
            return createResponse(session,
                "Hansı sahədə sizə kömək edə bilərəm?\n\n" +
                "• Data Analytics təlimləri\n" +
                "• Machine Learning təlimləri\n" +
                "• AI və Deep Learning\n" +
                "• Qeydiyyat və əlaqə");
        }
        
        // Step 3: Determine intent
        Intent intent = intentService.determineIntent(normalizedText);
        logger.info("Detected intent: {}", intent);
        
        // Step 4: Route based on intent
        switch (intent) {
            case GREETING:
                return createResponse(session,
                    "Salam! Data Science Academy-ə xoş gəlmisiniz! " +
                    "Sizə necə kömək edə bilərəm?\n\n" +
                    "• Təlimlərimiz haqqında məlumat\n" +
                    "• Qeydiyyat və konsultasiya\n" +
                    "• Əlaqə");
            
            case CONTACT:
                session.setCurrentMode(Mode.CONTACT.getValue());
                session.setCurrentStep("awaiting_name");
                return createResponse(session,
                    "Əlaqə üçün zəhmət olmasa ad və soyadınızı yazın");
            
            case CONSULT:
                session.setCurrentMode(Mode.CONSULT.getValue());
                session.setCurrentStep("awaiting_experience");
                return createResponse(session,
                    "Sizə uyğun təlimi seçməyə kömək edim.\n" +
                    "Hansı sahədə təcrübəniz var? " +
                    "(Məsələn: proqramlaşdırma, analitika, və ya yoxdur)");
            
            case QUERY:
            case TRAINER:
                // TODO: Step 2.x - Search in database
                return createResponse(session,
                    "Məlumatı axtarıram...\n" +
                    "(Database search - Step 2.x)");
            
            case UNCLEAR:
            default:
                return createResponse(session,
                    "Üzr istəyirik, məqsədinizi tam başa düşə bilmədim. " +
                    "Zəhmət olmasa daha konkret sual verin.\n\n" +
                    "Məsələn:\n" +
                    "• Python təlimi haqqında məlumat\n" +
                    "• Qeydiyyat üçün əlaqə\n" +
                    "• Təlim qiymətləri");
        }
    }
    
    private ChatResponse handleContactMode(SessionData session, String message) {
        // TODO: Step 3.1 - Full implementation
        String currentStep = session.getCurrentStep();
        
        if ("awaiting_name".equals(currentStep)) {
            session.putData("fullName", message);
            session.setCurrentStep("awaiting_phone");
            return createResponse(session,
                "Təşəkkürlər! İndi telefon nömrənizi yazın.\n" +
                "Format: +994XXXXXXXXX");
        }
        
        if ("awaiting_phone".equals(currentStep)) {
            // TODO: Validate phone format
            if (!message.matches("^\\+994[0-9]{9}$")) {
                return createResponse(session,
                    "Zəhmət olmasa düzgün formatda yazın: +994XXXXXXXXX");
            }
            
            session.putData("phone", message);
            session.setCurrentStep("awaiting_email");
            return createResponse(session,
                "Email ünvanınızı yazın (və ya keç demək üçün 'yox' yazın)");
        }
        
        if ("awaiting_email".equals(currentStep)) {
            // TODO: Step 3.1 - Save to database
            return createResponse(session,
                "Təşəkkürlər! Məlumatlarınız qeyd edildi. " +
                "Əməkdaşlarımız sizinlə əlaqə saxlayacaq.\n\n" +
                "Başqa sualınız varmı?");
        }
        
        return createResponse(session, "Gözlənilməz vəziyyət");
    }
    
    private ChatResponse handleConsultMode(SessionData session, String message) {
        // TODO: Step 3.2 - Full implementation
        String currentStep = session.getCurrentStep();
        
        if ("awaiting_experience".equals(currentStep)) {
            session.putData("experience", message);
            session.setCurrentStep("awaiting_interest");
            return createResponse(session,
                "Hansı sahəyə marağınız var?\n" +
                "(Məsələn: Data Analytics, Machine Learning, AI, Data Engineering)");
        }
        
        if ("awaiting_interest".equals(currentStep)) {
            session.putData("interest", message);
            session.setCurrentStep("awaiting_goal");
            return createResponse(session,
                "Məqsədiniz nədir?\n" +
                "(Məsələn: karyera dəyişikliyi, bilik artırma, sertifikat)");
        }
        
        if ("awaiting_goal".equals(currentStep)) {
            session.putData("goal", message);
            session.setCurrentStep("awaiting_time");
            return createResponse(session,
                "Nə qədər vaxtınız var?\n" +
                "(Məsələn: 2 ay, 3 ay, 6 ay)");
        }
        
        if ("awaiting_time".equals(currentStep)) {
            session.putData("time", message);
            session.setCurrentStep("awaiting_budget");
            return createResponse(session,
                "Büdcəniz nə qədərdir? (AZN)");
        }
        
        if ("awaiting_budget".equals(currentStep)) {
            session.putData("budget", message);
            // TODO: Step 3.3 - Search matching trainings
            return createResponse(session,
                "Sizə uyğun təlimləri axtarıram...\n" +
                "(Training search will be implemented in Step 3.3)");
        }
        
        return createResponse(session, "Gözlənilməz vəziyyət");
    }
    
    // ===== HELPER METHODS =====
    
    private boolean isGreeting(String message) {
        List<String> greetings = Arrays.asList(
            "salam", "salamlar", "sabah", "sabahınız xeyir", 
            "axşamınız xeyir", "gün aydın", "hello", "hi"
        );
        return greetings.stream().anyMatch(message::contains);
    }
    
    private boolean isContactIntent(String message) {
        List<String> contactKeywords = Arrays.asList(
            "əlaqə", "zəng", "telefon", "contact", 
            "müraciət", "əməkdaş"
        );
        return contactKeywords.stream().anyMatch(message::contains);
    }
    
    private boolean isConsultIntent(String message) {
        List<String> consultKeywords = Arrays.asList(
            "öyrənmək istəyirəm", "təlim", "kurs", 
            "öyrənmək", "məsləhət", "konsultasiya"
        );
        return consultKeywords.stream().anyMatch(message::contains);
    }
    
    private boolean isTrainingQuery(String message) {
        List<String> trainingKeywords = Arrays.asList(
            "python", "machine learning", "data science",
            "sql", "tableau", "power bi", "excel",
            "r proqramlaşdırma", "spss"
        );
        return trainingKeywords.stream()
                .anyMatch(keyword -> message.contains(keyword.toLowerCase()));
    }
    
    private ChatResponse createResponse(SessionData session, String reply) {
        return ChatResponse.builder()
                .sessionId(session.getSessionId())
                .reply(reply)
                .currentMode(session.getCurrentMode())
                .build();
    }
    
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) return "****";
        return sessionId.substring(0, 4) + "****" + 
               sessionId.substring(sessionId.length() - 4);
    }
}