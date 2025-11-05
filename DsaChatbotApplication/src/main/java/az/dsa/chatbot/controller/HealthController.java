package az.dsa.chatbot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides system health status for monitoring and load balancers
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Health check endpoint
     * Tests database connectivity and returns system status
     * 
     * @return Health status with database and timestamp info
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test database connection
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            health.put("status", "UP");
            health.put("database", "CONNECTED");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("database", "DISCONNECTED");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
        
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "DSA Chatbot");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Simple ping endpoint
     * Quick response without database check
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "alive");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}