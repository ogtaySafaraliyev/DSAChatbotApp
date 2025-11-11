package az.dsa.chatbot.service.impl;

import az.dsa.chatbot.dto.LeadRequest;
import az.dsa.chatbot.entity.Lead;
import az.dsa.chatbot.repository.LeadRepository;
import az.dsa.chatbot.service.LeadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class LeadServiceImpl implements LeadService {
    
    private static final Logger logger = LoggerFactory.getLogger(LeadServiceImpl.class);
    
    @Autowired
    private LeadRepository leadRepository;
    
    @Override
    @Transactional
    public Lead saveLead(String fullName, String phone, String email, String message) {
        logger.info("Saving lead - Phone: {}", maskPhone(phone));
        
        try {
            Lead lead = Lead.builder()
                    .fullName(fullName)
                    .phone(phone)
                    .email(email)
                    .message(message)
                    .source("chatbot")
                    .build();
            
            Lead saved = leadRepository.save(lead);
            logger.info("Lead saved successfully with ID: {}", saved.getId());
            
            return saved;
            
        } catch (Exception e) {
            logger.error("Error saving lead: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save lead", e);
        }
    }
    
    @Override
    @Transactional
    public Lead saveLead(LeadRequest request) {
        return saveLead(
            request.getFullName(),
            request.getPhone(),
            request.getEmail(),
            request.getMessage()
        );
    }
    
    @Override
    public boolean phoneExists(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        return leadRepository.existsByPhone(phone.trim());
    }
    
    @Override
    public long getTodayLeadCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return leadRepository.findRecentLeads(startOfDay).size();
    }
    
    // Helper method
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) return "****";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 2);
    }
}