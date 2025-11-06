package az.dsa.chatbot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LeadRequest {
    
    @NotBlank(message = "Ad və soyad daxil edilməlidir")
    @Size(min = 3, max = 100, message = "Ad və soyad 3-100 simvol arasında olmalıdır")
    private String fullName;
    
    @NotBlank(message = "Telefon nömrəsi daxil edilməlidir")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "Telefon formatı: +994XXXXXXXXX")
    private String phone;
    
    @Email(message = "Email düzgün formatda olmalıdır")
    private String email;
    
    @Size(max = 500, message = "Mesaj 500 simvoldan çox ola bilməz")
    private String message;
    
    // Constructors
    public LeadRequest() {}
    
    public LeadRequest(String fullName, String phone, String email, String message) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.message = message;
    }
    
    // Getters and Setters
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}