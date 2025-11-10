package az.dsa.chatbot.dto;

public class SearchFilters {
    
    private Integer minPrice;
    private Integer maxPrice;
    private String category; // "Data Analytics", "Machine Learning", etc.
    private Boolean activeOnly;
    private String source; // "FAQ", "TEXT", "TRAINING", or null for all
    
    // Constructors
    public SearchFilters() {
        this.activeOnly = true; // Default: only active trainings
    }
    
    // Getters and Setters
    public Integer getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }
    
    public Integer getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Boolean getActiveOnly() {
        return activeOnly;
    }
    
    public void setActiveOnly(Boolean activeOnly) {
        this.activeOnly = activeOnly;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
}