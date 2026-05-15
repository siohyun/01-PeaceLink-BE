package com.teamone.peacelink.app.translation;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String situationType;

    @Column(columnDefinition = "TEXT")
    private String originalMessage;

    @Column(columnDefinition = "TEXT")
    private String translatedMessage;

    @Column(nullable = false)
    private String targetLanguage;

    @Enumerated(EnumType.STRING)
    private OutputType outputType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum OutputType {
        TEXT, AUDIO, BOTH
    }
}