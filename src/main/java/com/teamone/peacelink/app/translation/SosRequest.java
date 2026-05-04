package com.teamone.peacelink.app.translation;

import com.teamone.peacelink.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SosRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double latitude;
    private Double longitude;
    private String statusCode;

    @Column(columnDefinition = "TEXT")
    private String originalMsg;

    @Column(columnDefinition = "TEXT")
    private String translatedMsg;

    private Boolean isResolved;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.isResolved = false;
        this.createdAt = LocalDateTime.now();
    }
}