package az.dsa.chatbot.controller;

import az.dsa.chatbot.config.ApiKeyConfig;
import az.dsa.chatbot.dto.ChatRequest;
import az.dsa.chatbot.dto.ChatResponse;
import az.dsa.chatbot.dto.ErrorResponse;
import az.dsa.chatbot.exception.RateLimitExceededException;
import az.dsa.chatbot.service.ChatService;
import az.dsa.chatbot.service.impl.SessionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@Tag(name = "Chat API", description = "Endpoints for chatbot interactions")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private SessionServiceImpl sessionService;
    
    @Autowired
    private ApiKeyConfig apiKeyConfig;
    
    /**
     * Main chat endpoint
     */
    @PostMapping("/message")
    @Operation(
        summary = "Send a message to the chatbot",
        description = "Process user message and return bot response. " +
                     "Supports three modes: Contact, Consult, and Query."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successful response",
            content = @Content(schema = @Schema(implementation = ChatResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid API key",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests - Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<ChatResponse> chat(
            @Parameter(description = "API Key (optional in development)", required = false)
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            
            @Parameter(description = "Chat request with sessionId and message", required = true)
            @Valid @RequestBody ChatRequest request,
            
            HttpServletRequest httpRequest) {
        
        String sessionId = request.getSessionId();
        
        try {
            logger.info("Received chat request - SessionId: {}", 
                       maskSessionId(sessionId));
            
            // Validate API key if authentication is enabled
            if (apiKeyConfig.isAuthenticationEnabled() && !apiKeyConfig.isValidApiKey(apiKey)) {
                logger.warn("Invalid API key from IP: {}", httpRequest.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse(sessionId, 
                              "Invalid or missing API key"));
            }
            
            // Check rate limiting
            if (sessionService.isRateLimitExceeded(sessionId)) {
                throw new RateLimitExceededException(
                    "⚠️ Çox sayda mesaj göndərildi. Zəhmət olmasa bir az gözləyin.\n\n" +
                    "Təcili sual üçün: 051 341 43 40"
                );
            }
            
            // Process message
            ChatResponse response = chatService.processMessage(request);
            
            logger.info("Chat response sent - SessionId: {}", 
                       maskSessionId(sessionId));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(sessionId, 
                          "Zəhmət olmasa məqsədinizi daha aydın yazın"));
                          
        } catch (RateLimitExceededException e) {
            logger.warn("Rate limit exceeded: {}", e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
            
        } catch (Exception e) {
            logger.error("Error processing chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(sessionId, 
                          "Üzr istəyirik, texniki problem yarandı. " +
                          "Əlaqə: 051 341 43 40 və ya info@dsa.az"));
        }
    }
    
    /**
     * Reset session
     */
    @DeleteMapping("/session/{sessionId}")
    @Operation(
        summary = "Reset a chat session",
        description = "Delete session data and start fresh"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Session reset successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> resetSession(
            @Parameter(description = "Session ID to reset", required = true)
            @PathVariable String sessionId) {
        try {
            logger.info("Resetting session: {}", maskSessionId(sessionId));
            chatService.resetSession(sessionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error resetting session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get session info
     */
    @GetMapping("/session/{sessionId}/info")
    @Operation(
        summary = "Get session information",
        description = "Retrieve basic information about a chat session"
    )
    public ResponseEntity<Object> getSessionInfo(
            @Parameter(description = "Session ID", required = true)
            @PathVariable String sessionId) {
        try {
            var session = sessionService.getSession(sessionId);
            
            if (session == null) {
                return ResponseEntity.notFound().build();
            }
            
            var info = new java.util.HashMap<String, Object>();
            info.put("sessionId", maskSessionId(sessionId));
            info.put("createdAt", session.getCreatedAt());
            info.put("lastActivity", session.getLastActivity());
            info.put("currentMode", session.getCurrentMode());
            info.put("messageCount", session.getConversationHistory().size());
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("Error getting session info: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ===== HELPER METHODS =====
    
    private boolean isSessionBlocked(String sessionId) {
        try {
            var sessionOpt = sessionService.getSession(sessionId);
            // Would need to add isBlocked to SessionData, or query directly from repository
            // For now, return false
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean shouldBlockSession(String sessionId) {
        // Simple heuristic: if session has too many errors in short time, block it
        // This is a placeholder - implement more sophisticated logic if needed
        return false;
    }
    
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