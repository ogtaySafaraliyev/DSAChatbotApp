package az.dsa.chatbot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    
    @Autowired
    private ApiKeyConfig apiKeyConfig;
    
    // Endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/health",
        "/api/ping",
        "/actuator/health",
        "/swagger-ui",
        "/api-docs",
        "/v3/api-docs"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip if authentication is disabled
        if (!apiKeyConfig.isAuthenticationEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get API key from header
        String apiKey = request.getHeader("X-API-Key");
        
        // Validate API key
        if (!apiKeyConfig.isValidApiKey(apiKey)) {
            logger.warn("Invalid API key attempt from IP: {}", request.getRemoteAddr());
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Invalid or missing API key\", " +
                "\"message\":\"Please provide a valid API key in X-API-Key header\"}"
            );
            return;
        }
        
        // API key is valid, continue
        filterChain.doFilter(request, response);
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
}