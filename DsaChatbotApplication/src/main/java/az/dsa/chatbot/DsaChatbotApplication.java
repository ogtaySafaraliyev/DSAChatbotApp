package az.dsa.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DSA Chatbot Application
 * Main entry point for the Spring Boot application
 * 
 * Features:
 * - Intelligent chatbot with OpenAI integration
 * - 3 modes: Contact, Consult, Query
 * - PostgreSQL data storage
 * - Session management
 * - Rate limiting
 * 
 * @author DSA Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class DsaChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsaChatbotApplication.class, args);
        System.out.println("\n" +
                "╔══════════════════════════════════════════╗\n" +
                "║   DSA Chatbot Started Successfully! ✓    ║\n" +
                "║                                          ║\n" +
                "║   API:     http://localhost:8080/api     ║\n" +
                "║   Swagger: http://localhost:8080/swagger ║\n" +
                "║   Health:  http://localhost:8080/health  ║\n" +
                "║                                          ║\n" +
                "║   Contact: 051 341 43 40                 ║\n" +
                "║   Email:   info@dsa.az                   ║\n" +
                "╚══════════════════════════════════════════╝\n");
    }
}