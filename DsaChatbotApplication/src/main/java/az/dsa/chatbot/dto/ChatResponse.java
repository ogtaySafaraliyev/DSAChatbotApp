package az.dsa.chatbot.dto;

import java.util.List;

public class ChatResponse {
    
    private String sessionId;
    private String reply;
    private String intent;
    private String currentMode;
    private List<String> suggestions;
    private boolean requiresInput;
    private String nextQuestion; // for consult mode
    
    // Constructors
    public ChatResponse() {}
    
    public ChatResponse(String sessionId, String reply) {
        this.sessionId = sessionId;
        this.reply = reply;
    }
    
    // Builder pattern for easy construction
    public static class Builder {
        private String sessionId;
        private String reply;
        private String intent;
        private String currentMode;
        private List<String> suggestions;
        private boolean requiresInput;
        private String nextQuestion;
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder reply(String reply) {
            this.reply = reply;
            return this;
        }
        
        public Builder intent(String intent) {
            this.intent = intent;
            return this;
        }
        
        public Builder currentMode(String currentMode) {
            this.currentMode = currentMode;
            return this;
        }
        
        public Builder suggestions(List<String> suggestions) {
            this.suggestions = suggestions;
            return this;
        }
        
        public Builder requiresInput(boolean requiresInput) {
            this.requiresInput = requiresInput;
            return this;
        }
        
        public Builder nextQuestion(String nextQuestion) {
            this.nextQuestion = nextQuestion;
            return this;
        }
        
        public ChatResponse build() {
            ChatResponse response = new ChatResponse();
            response.sessionId = this.sessionId;
            response.reply = this.reply;
            response.intent = this.intent;
            response.currentMode = this.currentMode;
            response.suggestions = this.suggestions;
            response.requiresInput = this.requiresInput;
            response.nextQuestion = this.nextQuestion;
            return response;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getReply() {
        return reply;
    }
    
    public void setReply(String reply) {
        this.reply = reply;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public String getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
    
    public boolean isRequiresInput() {
        return requiresInput;
    }
    
    public void setRequiresInput(boolean requiresInput) {
        this.requiresInput = requiresInput;
    }
    
    public String getNextQuestion() {
        return nextQuestion;
    }
    
    public void setNextQuestion(String nextQuestion) {
        this.nextQuestion = nextQuestion;
    }
}