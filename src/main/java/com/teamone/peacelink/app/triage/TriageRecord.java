package com.teamone.peacelink.app.triage;

import com.teamone.peacelink.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "triage_records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TriageRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String imageUrl;
    private String triageLevel; // RED, YELLOW, GREEN

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    @Column(columnDefinition = "TEXT")
    private String firstAidTip;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}