package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.ChatRequest;
import az.dsa.chatbot.dto.ChatResponse;
import az.dsa.chatbot.dto.SearchFilters;
import az.dsa.chatbot.dto.SearchResult;
import az.dsa.chatbot.dto.SessionData;
import az.dsa.chatbot.entity.Faq;
import az.dsa.chatbot.entity.Graduate;
import az.dsa.chatbot.entity.Text;
import az.dsa.chatbot.entity.Trainer;
import az.dsa.chatbot.entity.Training;
import az.dsa.chatbot.model.Intent;
import az.dsa.chatbot.model.Mode;
import az.dsa.chatbot.service.ChatService;
import az.dsa.chatbot.service.GraduateService;
import az.dsa.chatbot.service.IntentService;
import az.dsa.chatbot.service.LeadService;
import az.dsa.chatbot.service.OpenAIService;
import az.dsa.chatbot.service.RecommendationService;
import az.dsa.chatbot.service.SearchService;
import az.dsa.chatbot.service.SessionService;
import az.dsa.chatbot.service.TrainerService;
import az.dsa.chatbot.util.TrainingTextMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

	private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

	@Autowired
	private SessionService sessionService;

	@Autowired
	private OpenAIService openAIService;

	@Autowired
	private IntentService intentService;

	@Autowired
	private SearchService searchService;

	@Autowired
	private TrainingTextMapper trainingTextMapper;
	
	@Autowired
	private LeadService leadService;
	
	@Autowired
	private RecommendationService recommendationService;
	
	// my false addition check it later important ****************************************************
	// ***********************************************************************************************
	// ***********************************************************************************************
	// ***********************************************************************************************
	// ***********************************************************************************************
	// very important ********************************************************************************
	@Autowired(required = false)
	private TrainerService trainerService;

	@Autowired(required = false)
	private GraduateService graduateService;
	
	// ***********************************************************************************************
	// ***********************************************************************************************
	
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
			return createResponse(session, "Zəhmət olmasa məqsədinizi daha aydın yazın");
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
			return createResponse(session, "Hansı sahədə sizə kömək edə bilərəm?\n\n" + "• Data Analytics təlimləri\n"
					+ "• Machine Learning təlimləri\n" + "• AI və Deep Learning\n" + "• Qeydiyyat və əlaqə");
		}

		// Step 3: Determine intent
		Intent intent = intentService.determineIntent(normalizedText);
		logger.info("Detected intent: {}", intent);

		// Step 4: Route based on intent
		switch (intent) {
		case GREETING:
			return createResponse(session,
					"Salam! Data Science Academy-ə xoş gəlmisiniz! " + "Sizə necə kömək edə bilərəm?\n\n"
							+ "• Təlimlərimiz haqqında məlumat\n" + "• Qeydiyyat və konsultasiya\n" + "• Əlaqə");

		case CONTACT:
			session.setCurrentMode(Mode.CONTACT.getValue());
			session.setCurrentStep("awaiting_name");
			return createResponse(session, "Əlaqə üçün zəhmət olmasa ad və soyadınızı yazın");

		case CONSULT:
			session.setCurrentMode(Mode.CONSULT.getValue());
			session.setCurrentStep("awaiting_experience");
			return createResponse(session, "Sizə uyğun təlimi seçməyə kömək edim.\n" + "Hansı sahədə təcrübəniz var? "
					+ "(Məsələn: proqramlaşdırma, analitika, və ya yoxdur)");

		case QUERY:
		case TRAINER:
			return handleQueryIntent(session, normalizedText);

		case UNCLEAR:
		default:
			return createResponse(session,
					"Üzr istəyirik, məqsədinizi tam başa düşə bilmədim. " + "Zəhmət olmasa daha konkret sual verin.\n\n"
							+ "Məsələn:\n" + "• Python təlimi haqqında məlumat\n" + "• Qeydiyyat üçün əlaqə\n"
							+ "• Təlim qiymətləri");
		}
	}

	// ******************

	private ChatResponse handleQueryIntent(SessionData session, String normalizedText) {
	    logger.info("Handling query: {}", normalizedText);
	    
	    // Detect query type
	    String queryType = searchService.detectQueryType(normalizedText);
	    logger.debug("Query type detected: {}", queryType);
	    
	    switch (queryType) {
	        case "TRAINER":
	            return handleTrainerQuery(session, normalizedText);
	            
	        case "GRADUATE":
	            return handleGraduateQuery(session, normalizedText);
	            
	        case "BOOTCAMP":
	            return handleBootcampQuery(session);
	            
	        case "PRICE":
	            return handlePriceQuery(session, normalizedText);
	            
	        case "SCHEDULE":
	            return handleScheduleQuery(session, normalizedText);
	            
	        default:
	            return handleTrainingQuery(session, normalizedText);
	    }
	}
	
	private boolean isListRequest(String query) {
		String lower = query.toLowerCase();
		return lower.contains("hansı təlimlər") || lower.contains("bütün təlimlər") || lower.contains("təlimləriniz")
				|| lower.contains("kurslarınız") || lower.contains("siyahı");
	}

	private ChatResponse handleListRequest(SessionData session, String query) {
		logger.info("Handling list request");

		// Check if category-specific
		String category = searchService.detectCategory(query);

		List<SearchResult> results;
		if (category != null) {
			results = searchService.searchByCategory(category);
		} else {
			results = searchService.getPopularTrainings(10);
		}

		if (results.isEmpty()) {
			return createResponse(session, "Hal-hazırda aktiv təlim yoxdur. " + "Ətraflı məlumat üçün: 051 341 43 40");
		}

		StringBuilder response = new StringBuilder();
		if (category != null) {
			response.append(String.format("📚 **%s üzrə təlimlər:**\n\n", category));
		} else {
			response.append("📚 **Populyar təlimlərimiz:**\n\n");
		}

		for (int i = 0; i < Math.min(results.size(), 10); i++) {
			SearchResult result = results.get(i);
			response.append(String.format("%d. %s\n", i + 1, result.getTitle()));
		}

		response.append("\nKonkret təlim haqqında məlumat almaq üçün adını yaza bilərsiniz.");

		return createResponse(session, response.toString());
	}

	private Integer[] extractPriceRange(String query) {
		String lower = query.toLowerCase();

		// Check for price-related keywords
		if (!lower.contains("qiymət") && !lower.contains("azn") && !lower.contains("manat")
				&& !lower.contains("büdcə")) {
			return null;
		}

		// Try to extract numbers
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
		java.util.regex.Matcher matcher = pattern.matcher(query);

		List<Integer> numbers = new ArrayList<>();
		while (matcher.find()) {
			try {
				numbers.add(Integer.parseInt(matcher.group()));
			} catch (NumberFormatException e) {
				// Ignore
			}
		}

		if (numbers.isEmpty()) {
			return null;
		}

		// Determine min and max
		Integer min = null;
		Integer max = null;

		if (lower.contains("aşağı") || lower.contains("ucuz") || lower.contains("qədər")) {
			max = numbers.get(0);
		} else if (lower.contains("yuxarı") || lower.contains("bahalı") || lower.contains("dən")) {
			min = numbers.get(0);
		} else if (numbers.size() >= 2) {
			min = Math.min(numbers.get(0), numbers.get(1));
			max = Math.max(numbers.get(0), numbers.get(1));
		} else {
			max = numbers.get(0);
		}

		return new Integer[] { min, max };
	}

	private ChatResponse handlePriceQuery(SessionData session, Integer min, Integer max) {
		logger.info("Handling price query: {} - {}", min, max);

		List<SearchResult> results = searchService.searchByPriceRange(min, max);

		if (results.isEmpty()) {
			return createResponse(session,
					String.format("Bu qiymət aralığında (%s - %s AZN) təlim tapılmadı.\n" + "Əlaqə: 051 341 43 40",
							min != null ? min : "0", max != null ? max : "∞"));
		}

		StringBuilder response = new StringBuilder();
		response.append(String.format("💰 **Qiymət aralığı %s - %s AZN:**\n\n", min != null ? min : "0",
				max != null ? max : "∞"));

		for (int i = 0; i < Math.min(results.size(), 5); i++) {
			SearchResult result = results.get(i);
			if (result.getRawData() instanceof Text) {
				Text text = (Text) result.getRawData();
				response.append(String.format("• %s - %d AZN\n", text.getTitle(), text.getMoney()));
			}
		}

		return createResponse(session, response.toString());
	}

	private void enrichSearchResult(SearchResult result) {
		if ("TRAINING".equals(result.getSource()) && result.getRawData() instanceof Training) {
			Training training = (Training) result.getRawData();
			Text text = trainingTextMapper.getTextForTraining(training);

			if (text != null) {
				result.setContent(text.getDescription());
				// Store both for later use
				Map<String, Object> enriched = new HashMap<>();
				enriched.put("training", training);
				enriched.put("text", text);
				result.setRawData(enriched);
			}
		}
	}

	private String formatSearchResultResponse(SearchResult result, String query) {
		// Prepare raw data for OpenAI
		String rawData = buildRawData(result);

		// Format with OpenAI
		try {
			String formatted = openAIService.formatResponse(rawData, query);
			return formatted;
		} catch (Exception e) {
			logger.error("Error formatting response with OpenAI: {}", e.getMessage());
			// Fallback to manual formatting
			return formatManually(result);
		}
	}

	private String buildRawData(SearchResult result) {
		StringBuilder sb = new StringBuilder();

		if ("FAQ".equals(result.getSource()) && result.getRawData() instanceof Faq) {
			Faq faq = (Faq) result.getRawData();
			sb.append("Sual: ").append(faq.getQuestion()).append("\n");
			sb.append("Cavab: ").append(faq.getAnswer());

		} else if ("TEXT".equals(result.getSource()) && result.getRawData() instanceof Text) {
			Text text = (Text) result.getRawData();
			sb.append("Təlim: ").append(text.getTitle()).append("\n\n");

			if (text.getDescription() != null && !text.getDescription().isEmpty()) {
				sb.append("Təsvir: ").append(text.getDescription()).append("\n\n");
			}

			if (text.getMoney() != null) {
				sb.append("Qiymət: ").append(text.getMoney()).append(" AZN\n");
			}

			if (text.getInformation() != null && !text.getInformation().isEmpty()) {
				String info = text.getInformation();
				if (info.length() > 800) {
					info = info.substring(0, 800) + "...";
				}
				sb.append("\nƏtraflı məlumat:\n").append(info);
			}

		} else if (result.getRawData() instanceof Map) {
			// Enriched Training+Text data
			@SuppressWarnings("unchecked")
			Map<String, Object> enriched = (Map<String, Object>) result.getRawData();

			Training training = (Training) enriched.get("training");
			Text text = (Text) enriched.get("text");

			if (training != null) {
				sb.append("Təlim: ").append(training.getTitle()).append("\n\n");
			}

			if (text != null) {
				if (text.getDescription() != null) {
					sb.append("Təsvir: ").append(text.getDescription()).append("\n\n");
				}

				if (text.getMoney() != null) {
					sb.append("Qiymət: ").append(text.getMoney()).append(" AZN\n");
				}

				if (text.getInformation() != null) {
					String info = text.getInformation();
					if (info.length() > 800) {
						info = info.substring(0, 800) + "...";
					}
					sb.append("\nƏtraflı məlumat:\n").append(info);
				}
			}
		}

		return sb.toString();
	}

	private String formatManually(SearchResult result) {
		StringBuilder response = new StringBuilder();

		if ("FAQ".equals(result.getSource()) && result.getRawData() instanceof Faq) {
			Faq faq = (Faq) result.getRawData();
			response.append("✅ ").append(faq.getAnswer());

		} else if ("TEXT".equals(result.getSource()) && result.getRawData() instanceof Text) {
			Text text = (Text) result.getRawData();
			response.append("📚 **").append(text.getTitle()).append("**\n\n");

			if (text.getDescription() != null) {
				response.append(text.getDescription()).append("\n\n");
			}

			if (text.getMoney() != null) {
				response.append("💰 Qiymət: ").append(text.getMoney()).append(" AZN");
			}

		} else if ("TRAINING".equals(result.getSource())) {
			response.append("📚 ").append(result.getTitle());
		}

		return response.toString();
	}

	private String formatSearchResult(SearchResult result) {
		StringBuilder sb = new StringBuilder();

		sb.append("Mənbə: ").append(result.getSource()).append("\n");
		sb.append("Başlıq: ").append(result.getTitle()).append("\n");
		sb.append("Məzmun: ").append(result.getContent()).append("\n");

		// Add specific data based on source
		if ("FAQ".equals(result.getSource()) && result.getRawData() instanceof Faq) {
			Faq faq = (Faq) result.getRawData();
			sb.append("Cavab: ").append(faq.getAnswer());
		} else if ("TEXT".equals(result.getSource()) && result.getRawData() instanceof Text) {
			Text text = (Text) result.getRawData();
			if (text.getMoney() != null) {
				sb.append("Qiymət: ").append(text.getMoney()).append(" AZN\n");
			}
			if (text.getInformation() != null) {
				sb.append("Ətraflı: ")
						.append(text.getInformation().substring(0, Math.min(500, text.getInformation().length())));
			}
		}

		return sb.toString();
	}

	// *****************

	private ChatResponse handleContactMode(SessionData session, String message) {
	    String currentStep = session.getCurrentStep();
	    
	    if ("awaiting_name".equals(currentStep)) {
	        // Validate name
	        if (message.trim().length() < 3) {
	            return createResponse(session, 
	                "Zəhmət olmasa düzgün ad və soyad daxil edin (minimum 3 simvol)");
	        }
	        
	        session.putData("fullName", message.trim());
	        session.setCurrentStep("awaiting_phone");
	        return createResponse(session, 
	            "Təşəkkürlər! İndi telefon nömrənizi yazın.\n" +
	            "Format: +994XXXXXXXXX");
	    }
	    
	    if ("awaiting_phone".equals(currentStep)) {
	        // Validate phone format
	        String phone = message.trim();
	        if (!phone.matches("^\\+994[0-9]{9}$")) {
	            return createResponse(session, 
	                "❌ Telefon düzgün formatda deyil.\n" +
	                "Düzgün format: +994XXXXXXXXX\n" +
	                "Məsələn: +994501234567");
	        }
	        
	        // Check if phone already exists
	        if (leadService.phoneExists(phone)) {
	            logger.warn("Duplicate phone number attempted: {}", maskPhone(phone));
	            return createResponse(session,
	                "⚠️ Bu telefon nömrəsi artıq qeydiyyatdan keçib.\n" +
	                "Əməkdaşlarımız sizinlə əlaqə saxlayacaq.\n\n" +
	                "Başqa sualınız varmı?");
	        }
	        
	        session.putData("phone", phone);
	        session.setCurrentStep("awaiting_email");
	        return createResponse(session, 
	            "Email ünvanınızı yazın\n" +
	            "(və ya keçmək üçün 'yox' yazın)");
	    }
	    
	    if ("awaiting_email".equals(currentStep)) {
	        String email = null;
	        
	        // Check if user wants to skip
	        if (!message.trim().equalsIgnoreCase("yox")) {
	            // Validate email format
	            if (!message.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
	                return createResponse(session,
	                    "❌ Email formatı düzgün deyil.\n" +
	                    "Məsələn: example@gmail.com\n\n" +
	                    "Və ya keçmək üçün 'yox' yazın");
	            }
	            email = message.trim();
	        }
	        
	        session.putData("email", email);
	        session.setCurrentStep("awaiting_message");
	        return createResponse(session,
	            "Son addım! Qısa mesajınızı yazın:\n" +
	            "(Hansı təlim barədə məlumat almaq istəyirsiniz?)");
	    }
	    
	    if ("awaiting_message".equals(currentStep)) {
	        String userMessage = message.trim();
	        
	        // Save lead to database
	        try {
	            String fullName = session.getData("fullName");
	            String phone = session.getData("phone");
	            String email = session.getData("email");
	            
	            leadService.saveLead(fullName, phone, email, userMessage);
	            
	            logger.info("Lead saved successfully - Name: {}, Phone: {}", 
	                       fullName, maskPhone(phone));
	            
	            // Clear mode and collected data
	            session.setCurrentMode(null);
	            session.setCurrentStep(null);
	            session.clearData();
	            
	            return createResponse(session,
	                "✅ Təşəkkürlər! Məlumatlarınız uğurla qeyd edildi.\n\n" +
	                "📞 Əməkdaşlarımız tezliklə sizinlə əlaqə saxlayacaq.\n" +
	                "📧 Email: info@dsa.az\n" +
	                "☎️ Tel: 051 341 43 40\n\n" +
	                "Başqa sualınız varmı?");
	                
	        } catch (Exception e) {
	            logger.error("Failed to save lead: {}", e.getMessage(), e);
	            
	            // Don't clear session data in case of error
	            return createResponse(session,
	                "⚠️ Texniki problem yarandı. Zəhmət olmasa bir daha cəhd edin " +
	                "və ya birbaşa əlaqə saxlayın: 051 341 43 40");
	        }
	    }
	    
	    // Unexpected step
	    logger.warn("Unexpected step in contact mode: {}", currentStep);
	    session.setCurrentMode(null);
	    session.setCurrentStep(null);
	    return createResponse(session, 
	        "Üzr istəyirik, texniki problem yarandı. Yenidən başlayın.");
	}

	// Helper method (add to the class if not exists)
	private String maskPhone(String phone) {
	    if (phone == null || phone.length() < 8) return "****";
	    return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 2);
	}	
	

	private ChatResponse handleConsultMode(SessionData session, String message) {
	    String currentStep = session.getCurrentStep();
	    
	    if ("awaiting_experience".equals(currentStep)) {
	        session.putData("experience", message);
	        session.setCurrentStep("awaiting_interest");
	        return createResponse(session,
	            "Hansı sahəyə marağınız var?\n\n" +
	            "Məsələn:\n" +
	            "• Data Analytics (Excel, Tableau, Power BI)\n" +
	            "• Machine Learning\n" +
	            "• AI və Deep Learning\n" +
	            "• SQL və Data Engineering");
	    }
	    
	    if ("awaiting_interest".equals(currentStep)) {
	        session.putData("interest", message);
	        session.setCurrentStep("awaiting_goal");
	        return createResponse(session,
	            "Məqsədiniz nədir?\n\n" +
	            "Məsələn:\n" +
	            "• Karyera dəyişikliyi\n" +
	            "• Mövcud bilikləri inkişaf etdirmək\n" +
	            "• Sertifikat əldə etmək\n" +
	            "• İş tapmaq");
	    }
	    
	    if ("awaiting_goal".equals(currentStep)) {
	        session.putData("goal", message);
	        session.setCurrentStep("awaiting_time");
	        return createResponse(session,
	            "Təlimə nə qədər vaxt ayıra bilərsiniz?\n\n" +
	            "Məsələn: 2 ay, 3 ay, 6 ay");
	    }
	    
	    if ("awaiting_time".equals(currentStep)) {
	        session.putData("time", message);
	        session.setCurrentStep("awaiting_budget");
	        return createResponse(session,
	            "Büdcəniz nə qədərdir? (AZN)\n\n" +
	            "Qiymət aralığımız: 250 AZN - 2000 AZN\n" +
	            "Məsələn: 500, 1000, 1500");
	    }
	    
	    if ("awaiting_budget".equals(currentStep)) {
	        session.putData("budget", message);
	        
	        logger.info("Starting recommendation generation for session: {}", 
	                   maskSessionId(session.getSessionId()));
	        
	        try {
	            // Get recommendations using the session data
	            List<SearchResult> recommendations = 
	                recommendationService.getRecommendations(session);
	            
	            // Format response
	            String formattedResponse = 
	                recommendationService.formatRecommendations(recommendations, session);
	            
	            // Clear mode and data
	            session.setCurrentMode(null);
	            session.setCurrentStep(null);
	            session.clearData();
	            
	            logger.info("Recommendations generated successfully - Count: {}", 
	                       recommendations.size());
	            
	            return createResponse(session, formattedResponse);
	            
	        } catch (Exception e) {
	            logger.error("Error generating recommendations: {}", e.getMessage(), e);
	            
	            // Clear mode on error
	            session.setCurrentMode(null);
	            session.setCurrentStep(null);
	            session.clearData();
	            
	            return createResponse(session,
	                "⚠️ Tövsiyələri hazırlayarkən texniki problem yarandı.\n" +
	                "Zəhmət olmasa bir daha cəhd edin və ya birbaşa əlaqə saxlayın:\n\n" +
	                "📞 051 341 43 40\n" +
	                "📧 info@dsa.az");
	        }
	    }
	    
	    // Unexpected step
	    logger.warn("Unexpected step in consult mode: {}", currentStep);
	    session.setCurrentMode(null);
	    session.setCurrentStep(null);
	    return createResponse(session,
	        "Üzr istəyirik, texniki problem yarandı. Yenidən başlayın.");
	}
	// ===== HELPER METHODS =====

	private boolean isGreeting(String message) {
		List<String> greetings = Arrays.asList("salam", "salamlar", "sabah", "sabahınız xeyir", "axşamınız xeyir",
				"gün aydın", "hello", "hi");
		return greetings.stream().anyMatch(message::contains);
	}

	private boolean isContactIntent(String message) {
		List<String> contactKeywords = Arrays.asList("əlaqə", "zəng", "telefon", "contact", "müraciət", "əməkdaş");
		return contactKeywords.stream().anyMatch(message::contains);
	}

	private boolean isConsultIntent(String message) {
		List<String> consultKeywords = Arrays.asList("öyrənmək istəyirəm", "təlim", "kurs", "öyrənmək", "məsləhət",
				"konsultasiya");
		return consultKeywords.stream().anyMatch(message::contains);
	}

	private boolean isTrainingQuery(String message) {
		List<String> trainingKeywords = Arrays.asList("python", "machine learning", "data science", "sql", "tableau",
				"power bi", "excel", "r proqramlaşdırma", "spss");
		return trainingKeywords.stream().anyMatch(keyword -> message.contains(keyword.toLowerCase()));
	}

	private ChatResponse createResponse(SessionData session, String reply) {
		return ChatResponse.builder().sessionId(session.getSessionId()).reply(reply)
				.currentMode(session.getCurrentMode()).build();
	}

	private String maskSessionId(String sessionId) {
		if (sessionId == null || sessionId.length() < 8)
			return "****";
		return sessionId.substring(0, 4) + "****" + sessionId.substring(sessionId.length() - 4);
	}
	
	
	//addition new methods after phase  3.3
	
	// NEW: Handle trainer queries
	private ChatResponse handleTrainerQuery(SessionData session, String query) {
	    logger.debug("Handling trainer query");
	    
	    // Extract trainer name if mentioned
	    String keyword = extractKeyword(query);
	    
	    List<Trainer> trainers = trainerService.searchTrainers(keyword);
	    
	    if (trainers.isEmpty()) {
	        trainers = trainerService.getAllTrainers();
	    }
	    
	    String response = trainerService.formatTrainerInfo(trainers);
	    return createResponse(session, response);
	}

	// NEW: Handle graduate queries
	private ChatResponse handleGraduateQuery(SessionData session, String query) {
	    logger.debug("Handling graduate query");
	    
	    String keyword = extractKeyword(query);
	    
	    List<Graduate> graduates = graduateService.searchGraduates(keyword);
	    
	    if (graduates.isEmpty()) {
	        graduates = graduateService.getRandomSuccessStories(6);
	    }
	    
	    String response = graduateService.formatGraduateInfo(graduates);
	    return createResponse(session, response);
	}

	// NEW: Handle bootcamp structure queries
	private ChatResponse handleBootcampQuery(SessionData session) {
	    logger.debug("Handling bootcamp structure query");
	    
	    String response = searchService.getBootcampStructure();
	    return createResponse(session, response);
	}

	// NEW: Handle price queries
	private ChatResponse handlePriceQuery(SessionData session, String query) {
	    logger.debug("Handling price query");
	    
	    // Check if specific training mentioned
	    String keyword = extractKeyword(query);
	    
	    // Extract price range if specified
	    Integer[] priceRange = extractPriceRange(query);
	    
	    List<SearchResult> results;
	    
	    if (priceRange != null) {
	        results = searchService.searchByPriceRange(priceRange[0], priceRange[1]);
	    } else if (keyword != null && !keyword.isEmpty()) {
	        results = searchService.searchTrainingsDetailed(keyword);
	    } else {
	        results = searchService.getPopularTrainings(10);
	    }
	    
	    String response = searchService.formatPriceInfo(results);
	    return createResponse(session, response);
	}

	// NEW: Handle schedule queries
	private ChatResponse handleScheduleQuery(SessionData session, String query) {
	    logger.debug("Handling schedule query");
	    
	    String keyword = extractKeyword(query);
	    
	    List<SearchResult> results = searchService.searchTrainingsDetailed(keyword);
	    
	    if (results.isEmpty()) {
	        return createResponse(session,
	            "📅 **Təlim cədvəlləri:**\n\n" +
	            "Təlimlər həftədə 2-3 dəfə keçirilir.\n" +
	            "Həm həftə içi, həm də həftə sonu qruplarımız var.\n\n" +
	            "📞 Konkret təlim cədvəli üçün: 051 341 43 40\n" +
	            "📧 Email: info@dsa.az");
	    }
	    
	    // Format response with training info
	    StringBuilder response = new StringBuilder();
	    response.append("📅 **Təlim cədvəlləri:**\n\n");
	    
	    for (int i = 0; i < Math.min(results.size(), 3); i++) {
	        SearchResult result = results.get(i);
	        response.append(String.format("📚 %s\n", result.getTitle()));
	        response.append("   ⏰ Həftədə 2-3 dəfə, 2-3 saat\n");
	        response.append("   📍 Online və ya офлайн formatda\n\n");
	    }
	    
	    response.append("📞 Dəqiq tarix və saat üçün: 051 341 43 40");
	    
	    return createResponse(session, response.toString());
	}

	// NEW: Handle general training queries (existing logic enhanced)
	private ChatResponse handleTrainingQuery(SessionData session, String query) {
	    logger.debug("Handling training query");
	    
	    // Detect if user is asking for categories/list
	    if (isListRequest(query)) {
	        return handleListRequest(session, query);
	    }
	    
	    // Detect category
	    String category = searchService.detectCategory(query);
	    if (category != null) {
	        logger.debug("Detected category: {}", category);
	    }
	    
	    // Prepare filters
	    SearchFilters filters = new SearchFilters();
	    filters.setActiveOnly(true);
	    if (category != null) {
	        filters.setCategory(category);
	    }
	    
	    // Search with filters
	    List<SearchResult> results = searchService.searchWithFilters(query, filters);
	    
	    // Fallback to fuzzy search if no results
	    if (results.isEmpty()) {
	        results = searchService.fuzzySearch(query, 3);
	    }
	    
	    if (results.isEmpty()) {
	        return createResponse(session,
	            "Bu məlumatı əməkdaşlarımızdan öyrənə bilərik.\n\n" +
	            "📞 Əlaqə: 051 341 43 40\n" +
	            "📧 Email: info@dsa.az\n\n" +
	            "Başqa sualınız varmı?");
	    }
	    
	    // Get the best result
	    SearchResult bestResult = results.get(0);
	    
	    // Enrich result
	    enrichSearchResult(bestResult);
	    
	    // Format response
	    String formattedResponse = formatSearchResultResponse(bestResult, query);
	    
	    // Add additional results if available
	    if (results.size() > 1) {
	        formattedResponse += "\n\n📚 Digər uyğun təlimlər:";
	        for (int i = 1; i < Math.min(results.size(), 3); i++) {
	            SearchResult result = results.get(i);
	            formattedResponse += String.format("\n• %s", result.getTitle());
	        }
	        formattedResponse += "\n\nDaha ətraflı məlumat üçün konkret təlim adını yaza bilərsiniz.";
	    }
	    
	    return createResponse(session, formattedResponse);
	}

	// Helper method to extract main keyword from query
	private String extractKeyword(String query) {
	    if (query == null) return "";
	    
	    // Remove common question words
	    String cleaned = query.toLowerCase()
	        .replaceAll("\\b(hansı|nə|kim|necə|haqqında|üçün|barədə|məlumat)\\b", "")
	        .trim();
	    
	    // Get main words (skip short words)
	    String[] words = cleaned.split("\\s+");
	    for (String word : words) {
	        if (word.length() > 3) {
	            return word;
	        }
	    }
	    
	    return cleaned;
	}
	
}