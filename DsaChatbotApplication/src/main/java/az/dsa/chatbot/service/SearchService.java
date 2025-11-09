package az.dsa.chatbot.service;

import az.dsa.chatbot.dto.SearchResult;
import java.util.List;

public interface SearchService {
    
    /**
     * Search in FAQ table
     */
    List<SearchResult> searchFAQ(String query);
    
    /**
     * Search in Text (metinler) table
     */
    List<SearchResult> searchText(String query);
    
    /**
     * Search in all tables and return top results
     */
    List<SearchResult> searchAll(String query, int limit);
    
    /**
     * Extract keywords from query
     */
    List<String> extractKeywords(String query);
    
    /**
     * Calculate relevance score
     */
    double calculateRelevance(String query, String content);
    
    //new ones ******
    
    /**
     * Search in Training table
     */
    List<SearchResult> searchTraining(String query);

    /**
     * Search with fuzzy matching for typos
     */
    List<SearchResult> fuzzySearch(String query, int limit);

    /**
     * Search by price range
     */
    List<SearchResult> searchByPriceRange(Integer minPrice, Integer maxPrice);
    
}