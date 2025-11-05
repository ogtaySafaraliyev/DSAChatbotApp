package az.dsa.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "graduates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Graduate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "work_position", length = 500)
    private String workPosition;
    
    @Column(name = "work_location", length = 500)
    private String workLocation;
    
    @Column(length = 500)
    private String image;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
