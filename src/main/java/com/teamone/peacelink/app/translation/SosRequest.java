package com.teamone.peacelink.app.translation;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sos_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "situation_type", nullable = false)
    private String situationType;

    @Column(name = "original_message", columnDefinition = "TEXT")
    private String originalMessage;

    @Column(name = "translated_message", columnDefinition = "TEXT")
    private String translatedMessage;

    @Column(name = "target_language", nullable = false)
    private String targetLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "output_type", nullable = false)
    private OutputType outputType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum OutputType {
        TEXT, AUDIO, BOTH
    }
}