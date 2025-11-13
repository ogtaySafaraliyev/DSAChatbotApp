package az.dsa.chatbot.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {
    
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;
    
    // Constructors
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
        this.details = new ArrayList<>();
    }
    
    public ErrorResponse(int status, String error, String message) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
    }
    
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message);
        this.path = path;
    }
    
    // Builder pattern
    public static class Builder {
        private ErrorResponse response;
        
        public Builder() {
            response = new ErrorResponse();
        }
        
        public Builder status(int status) {
            response.status = status;
            return this;
        }
        
        public Builder error(String error) {
            response.error = error;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder path(String path) {
            response.path = path;
            return this;
        }
        
        public Builder addDetail(String detail) {
            response.details.add(detail);
            return this;
        }
        
        public ErrorResponse build() {
            return response;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public List<String> getDetails() {
        return details;
    }
    
    public void setDetails(List<String> details) {
        this.details = details;
    }
}