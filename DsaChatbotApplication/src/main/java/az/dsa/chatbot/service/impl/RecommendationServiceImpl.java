package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.SearchResult;
import az.dsa.chatbot.dto.SessionData;
import az.dsa.chatbot.entity.Text;
import az.dsa.chatbot.entity.Training;
import az.dsa.chatbot.repository.TrainingRepository;
import az.dsa.chatbot.service.RecommendationService;
import az.dsa.chatbot.util.TrainingTextMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    
    @Autowired
    private TrainingRepository trainingRepository;
    
    @Autowired
    private TrainingTextMapper trainingTextMapper;
    
    // Category keywords
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
        "Data Analytics", Arrays.asList("analytics", "analitika", "tableau", "power bi", "excel", "data"),
        "Machine Learning", Arrays.asList("machine", "ml", "maşın", "python", "r"),
        "Deep Learning", Arrays.asList("deep", "ai", "süni", "neural", "nlp"),
        "Data Engineering", Arrays.asList("sql", "databaza", "database", "mühəndislik"),
        "Programming", Arrays.asList("proqramlaşdırma", "kod", "python", "django")
    );
    
    @Override
    public List<SearchResult> getRecommendations(SessionData session) {
        // Extract data from session
        String experience = session.getData("experience");
        String interest = session.getData("interest");
        String budgetStr = session.getData("budget");
        
        logger.info("Generating recommendations - Experience: {}, Interest: {}, Budget: {}", 
                   experience, interest, budgetStr);
        
        // Parse values
        String experienceLevel = parseExperienceLevel(experience);
        String category = parseCategory(interest);
        Integer budget = parseBudget(budgetStr);
        
        // Get all active trainings
        List<Training> allTrainings = trainingRepository.findAllActive();
        List<SearchResult> results = new ArrayList<>();
        
        for (Training training : allTrainings) {
            Text textDetails = trainingTextMapper.getTextForTraining(training);
            if (textDetails == null) continue;
            
            // Calculate match score
            double score = calculateMatchScore(training, textDetails, 
                                              experienceLevel, category, budget);
            
            // Create search result
            SearchResult result = new SearchResult();
            result.setSource("TRAINING");
            result.setId(training.getId());
            result.setTitle(training.getTitle());
            result.setContent(textDetails.getDescription());
            result.setRelevanceScore(score);
            result.setRawData(textDetails);
            
            results.add(result);
        }
        
        // Sort by score and return top 3
        return results.stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(3)
                .collect(Collectors.toList());
    }
    
    private double calculateMatchScore(Training training, Text textDetails, 
                                      String experienceLevel, String category, Integer budget) {
        double score = 0.0;
        
        String trainingTitle = training.getTitle().toLowerCase();
        String description = textDetails.getDescription() != null ? 
                           textDetails.getDescription().toLowerCase() : "";
        
        // 1. Category match (40 points)
        if (category != null) {
            List<String> keywords = CATEGORY_KEYWORDS.getOrDefault(category, Collections.emptyList());
            for (String keyword : keywords) {
                if (trainingTitle.contains(keyword) || description.contains(keyword)) {
                    score += 40;
                    break;
                }
            }
        }
        
        // 2. Experience level match (30 points)
        if ("beginner".equals(experienceLevel)) {
            if (trainingTitle.contains("excel") || trainingTitle.contains("sql") || 
                trainingTitle.contains("python")) {
                score += 30;
            }
        } else if ("advanced".equals(experienceLevel)) {
            if (trainingTitle.contains("machine") || trainingTitle.contains("deep") || 
                trainingTitle.contains("nlp")) {
                score += 30;
            }
        } else {
            score += 15;
        }
        
        // 3. Budget match (30 points)
        if (budget != null && textDetails.getMoney() != null) {
            if (textDetails.getMoney() <= budget) {
                score += 30;
            } else {
                // Penalty for over budget
                double overBudget = textDetails.getMoney() - budget;
                score -= Math.min(20, overBudget / 50);
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    @Override
    public String formatRecommendations(List<SearchResult> recommendations, SessionData session) {
        if (recommendations == null || recommendations.isEmpty()) {
            return "Təəssüf ki, tələblərinizə tam uyğun təlim tapılmadı.\n\n" +
                   "📞 Ətraflı məsləhət üçün: 051 341 43 40\n" +
                   "📧 Email: info@dsa.az";
        }
        
        StringBuilder response = new StringBuilder();
        response.append("✅ Sizə uyğun təlimlər:\n\n");
        
        for (int i = 0; i < recommendations.size(); i++) {
            SearchResult rec = recommendations.get(i);
            Text text = (Text) rec.getRawData();
            
            response.append(String.format("📚 **%d. %s**\n", i + 1, rec.getTitle()));
            response.append(String.format("   Uyğunluq: %.0f%%\n", rec.getRelevanceScore()));
            
            if (text != null && text.getMoney() != null) {
                response.append(String.format("   💰 Qiymət: %d AZN\n", text.getMoney()));
            }
            
            if (text != null && text.getDescription() != null) {
                String shortDesc = text.getDescription();
                if (shortDesc.length() > 100) {
                    shortDesc = shortDesc.substring(0, 100) + "...";
                }
                response.append(String.format("   📝 %s\n", shortDesc));
            }
            
            response.append("\n");
        }
        
        response.append("🔍 Konkret təlim haqqında ətraflı məlumat üçün adını yaza bilərsiniz.\n");
        response.append("📞 Qeydiyyat: 051 341 43 40");
        
        return response.toString();
    }
    
    // Helper methods
    private String parseExperienceLevel(String input) {
        if (input == null) return "beginner";
        
        String lower = input.toLowerCase();
        
        if (lower.contains("yoxdur") || lower.contains("başlanğıc") || 
            lower.contains("yeni") || lower.contains("bilmirəm")) {
            return "beginner";
        }
        
        if (lower.contains("yüksək") || lower.contains("professional") || 
            lower.contains("təcrübəli") || lower.contains("işləmişəm")) {
            return "advanced";
        }
        
        return "intermediate";
    }
    
    private String parseCategory(String input) {
        if (input == null) return null;
        
        String lower = input.toLowerCase();
        
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        
        return null;
    }
    
    private Integer parseBudget(String input) {
        if (input == null) return null;
        
        // Extract numbers
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse budget: {}", input);
            }
        }
        
        return null;
    }
}