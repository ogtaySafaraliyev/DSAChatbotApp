package az.dsa.chatbot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metinler")
public class Text {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500, nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String information;
    
    private Integer money;
    
    @Column(length = 500)
    private String image;
    
    @Column(name = "for_who", columnDefinition = "TEXT")
    private String forWho;
    
    @Column(columnDefinition = "TEXT")
    private String certificates;
    
    @Column(name = "certificate_image", length = 500)
    private String certificateImage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "trainings_id")
    private Integer trainingsId;
    
    // Constructors
    public Text() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getInformation() {
        return information;
    }
    
    public void setInformation(String information) {
        this.information = information;
    }
    
    public Integer getMoney() {
        return money;
    }
    
    public void setMoney(Integer money) {
        this.money = money;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getForWho() {
        return forWho;
    }
    
    public void setForWho(String forWho) {
        this.forWho = forWho;
    }
    
    public String getCertificates() {
        return certificates;
    }
    
    public void setCertificates(String certificates) {
        this.certificates = certificates;
    }
    
    public String getCertificateImage() {
        return certificateImage;
    }
    
    public void setCertificateImage(String certificateImage) {
        this.certificateImage = certificateImage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getTrainingsId() {
        return trainingsId;
    }
    
    public void setTrainingsId(Integer trainingsId) {
        this.trainingsId = trainingsId;
    }
}