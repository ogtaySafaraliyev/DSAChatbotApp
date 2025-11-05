package az.dsa.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "training_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String information;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 500)
    private String image;
    
    @Column(name = "for_who", columnDefinition = "TEXT")
    private String forWho;
    
    @Column(columnDefinition = "TEXT")
    private String certificates;
    
    @Column(name = "certificate_image", length = 500)
    private String certificateImage;
    
    @OneToOne
    @JoinColumn(name = "training_id")
    private Training training;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}