package az.dsa.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {
    
    @NotBlank(message = "Session ID boş ola bilməz")
    private String sessionId;
    
    @NotBlank(message = "Mesaj boş ola bilməz")
    @Size(max = 1000, message = "Mesaj 1000 simvoldan çox ola bilməz")
    private String message;
    
    private String mode; // optional: contact, consult, query
    
    // Constructors
    public ChatRequest() {}
    
    public ChatRequest(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMode() {
        return mode;
    }
    
    public void setMode(String mode) {
        this.mode = mode;
    }
}