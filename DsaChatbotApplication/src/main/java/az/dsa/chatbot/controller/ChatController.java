package az.dsa.chatbot.controller;

import az.dsa.chatbot.dto.ChatRequest;
import az.dsa.chatbot.dto.ChatResponse;
import az.dsa.chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Configure properly in production
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ChatService chatService;
    
    /**
     * Main chat endpoint
     * POST /api/chat/message
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody ChatRequest request) {
        
        try {
            logger.info("Received chat request - SessionId: {}", 
                       maskSessionId(request.getSessionId()));
            
            // TODO: Validate API key (Phase 4)
            
            ChatResponse response = chatService.processMessage(request);
            
            logger.info("Chat response sent - SessionId: {}", 
                       maskSessionId(request.getSessionId()));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(request.getSessionId(), 
                          "Zəhmət olmasa məqsədinizi daha aydın yazın"));
                          
        } catch (Exception e) {
            logger.error("Error processing chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(request.getSessionId(), 
                          "Üzr istəyirik, texniki problem yarandı. " +
                          "Əlaqə: 051 341 43 40 və ya info@dsa.az"));
        }
    }
    
    /**
     * Reset session
     * DELETE /api/chat/session/{sessionId}
     */
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> resetSession(@PathVariable String sessionId) {
        try {
            logger.info("Resetting session: {}", maskSessionId(sessionId));
            chatService.resetSession(sessionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error resetting session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper methods
    private ChatResponse createErrorResponse(String sessionId, String errorMessage) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .reply(errorMessage)
                .build();
    }
    
    private String maskSessionId(String sessionId) {
        if (sessionId == null || sessionId.length() < 8) return "****";
        return sessionId.substring(0, 4) + "****" + 
               sessionId.substring(sessionId.length() - 4);
    }
}