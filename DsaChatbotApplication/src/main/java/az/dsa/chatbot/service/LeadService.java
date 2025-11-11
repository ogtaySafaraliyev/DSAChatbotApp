package az.dsa.chatbot.service;

import az.dsa.chatbot.dto.LeadRequest;
import az.dsa.chatbot.entity.Lead;

public interface LeadService {
    
    /**
     * Save lead from chatbot interaction
     */
    Lead saveLead(String fullName, String phone, String email, String message);
    
    /**
     * Save lead from DTO
     */
    Lead saveLead(LeadRequest request);
    
    /**
     * Check if phone already exists (to avoid duplicates)
     */
    boolean phoneExists(String phone);
    
    /**
     * Get lead count for today
     */
    long getTodayLeadCount();
}