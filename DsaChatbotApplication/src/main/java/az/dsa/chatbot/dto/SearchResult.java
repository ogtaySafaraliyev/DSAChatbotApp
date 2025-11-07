package az.dsa.chatbot.dto;

public class SearchResult {
    
    private String source; // "FAQ", "TEXT", "TRAINING"
    private Long id;
    private String title;
    private String content;
    private double relevanceScore;
    private Object rawData; // Original entity
    
    // Constructors
    public SearchResult() {}
    
    public SearchResult(String source, Long id, String title, String content, double relevanceScore) {
        this.source = source;
        this.id = id;
        this.title = title;
        this.content = content;
        this.relevanceScore = relevanceScore;
    }
    
    // Getters and Setters
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
    
    public Object getRawData() {
        return rawData;
    }
    
    public void setRawData(Object rawData) {
        this.rawData = rawData;
    }
}