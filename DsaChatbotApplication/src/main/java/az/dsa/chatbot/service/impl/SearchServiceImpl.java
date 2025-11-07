package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.SearchResult;
import az.dsa.chatbot.entity.Faq;
import az.dsa.chatbot.entity.Text;
import az.dsa.chatbot.repository.FaqRepository;
import az.dsa.chatbot.repository.TextRepository;
import az.dsa.chatbot.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);
    
    @Autowired
    private FaqRepository faqRepository;
    
    @Autowired
    private TextRepository textRepository;
    
    // Common stop words in Azerbaijani
    private static final Set<String> STOP_WORDS = Set.of(
        "və", "ilə", "üçün", "bir", "bu", "o", "ki", "nə", 
        "necə", "hansı", "haqqında", "üzrə", "kimi", "da", "də"
    );
    
    @Override
    public List<SearchResult> searchFAQ(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        logger.debug("Searching FAQ for: {}", query);
        
        List<String> keywords = extractKeywords(query);
        List<SearchResult> results = new ArrayList<>();
        
        // Search for each keyword
        for (String keyword : keywords) {
            List<Faq> faqs = faqRepository.searchByKeyword(keyword);
            
            for (Faq faq : faqs) {
                // Calculate relevance score
                String searchableContent = faq.getQuestion() + " " + faq.getAnswer();
                double score = calculateRelevance(query, searchableContent);
                
                SearchResult result = new SearchResult();
                result.setSource("FAQ");
                result.setId(faq.getId());
                result.setTitle(faq.getQuestion());
                result.setContent(faq.getAnswer());
                result.setRelevanceScore(score);
                result.setRawData(faq);
                
                results.add(result);
            }
        }
        
        // Remove duplicates and sort by relevance
        List<SearchResult> uniqueResults = results.stream()
                .collect(Collectors.toMap(
                    r -> r.getId(),
                    r -> r,
                    (existing, replacement) -> 
                        existing.getRelevanceScore() > replacement.getRelevanceScore() 
                            ? existing : replacement
                ))
                .values()
                .stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());
        
        logger.debug("Found {} unique FAQ results", uniqueResults.size());
        return uniqueResults;
    }
    
    @Override
    public List<SearchResult> searchText(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        logger.debug("Searching Text for: {}", query);
        
        List<String> keywords = extractKeywords(query);
        List<SearchResult> results = new ArrayList<>();
        
        for (String keyword : keywords) {
            List<Text> texts = textRepository.searchByKeyword(keyword);
            
            for (Text text : texts) {
                // Calculate relevance score
                String searchableContent = text.getTitle() + " " + 
                                          text.getDescription() + " " + 
                                          text.getInformation();
                double score = calculateRelevance(query, searchableContent);
                
                SearchResult result = new SearchResult();
                result.setSource("TEXT");
                result.setId(text.getId());
                result.setTitle(text.getTitle());
                result.setContent(text.getDescription()); // Use description as preview
                result.setRelevanceScore(score);
                result.setRawData(text);
                
                results.add(result);
            }
        }
        
        // Remove duplicates and sort
        List<SearchResult> uniqueResults = results.stream()
                .collect(Collectors.toMap(
                    r -> r.getId(),
                    r -> r,
                    (existing, replacement) -> 
                        existing.getRelevanceScore() > replacement.getRelevanceScore() 
                            ? existing : replacement
                ))
                .values()
                .stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());
        
        logger.debug("Found {} unique Text results", uniqueResults.size());
        return uniqueResults;
    }
    
    @Override
    public List<SearchResult> searchAll(String query, int limit) {
        logger.debug("Searching all tables for: {}", query);
        
        List<SearchResult> allResults = new ArrayList<>();
        
        // Search in FAQ
        allResults.addAll(searchFAQ(query));
        
        // Search in Text
        allResults.addAll(searchText(query));
        
        // Sort by relevance and limit
        List<SearchResult> topResults = allResults.stream()
                .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .limit(limit)
                .collect(Collectors.toList());
        
        logger.info("Found {} total results, returning top {}", 
                   allResults.size(), topResults.size());
        
        return topResults;
    }
    
    @Override
    public List<String> extractKeywords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Normalize and split
        String normalized = query.toLowerCase()
                .replaceAll("[.,!?;:]", " ")
                .trim();
        
        String[] words = normalized.split("\\s+");
        
        // Filter stop words and short words
        List<String> keywords = Arrays.stream(words)
                .filter(word -> word.length() > 2)
                .filter(word -> !STOP_WORDS.contains(word))
                .distinct()
                .collect(Collectors.toList());
        
        logger.debug("Extracted keywords: {}", keywords);
        return keywords;
    }
    
    @Override
    public double calculateRelevance(String query, String content) {
        if (query == null || content == null) {
            return 0.0;
        }
        
        String queryLower = query.toLowerCase();
        String contentLower = content.toLowerCase();
        
        // Extract keywords from query
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // Exact phrase match (highest score)
        if (contentLower.contains(queryLower)) {
            score += 10.0;
        }
        
        // Count keyword matches
        int matchCount = 0;
        for (String keyword : keywords) {
            if (contentLower.contains(keyword)) {
                matchCount++;
                
                // Bonus for title match
                if (content.length() < 200 && contentLower.startsWith(keyword)) {
                    score += 3.0;
                } else {
                    score += 2.0;
                }
            }
        }
        
        // Keyword coverage ratio
        double coverage = (double) matchCount / keywords.size();
        score += coverage * 5.0;
        
        // Bonus for multiple keyword matches
        if (matchCount > 1) {
            score += matchCount * 1.5;
        }
        
        return score;
    }
}