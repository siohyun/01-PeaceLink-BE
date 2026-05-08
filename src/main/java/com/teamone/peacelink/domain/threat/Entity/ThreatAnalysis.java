package com.teamone.peacelink.domain.threat.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "threat_analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThreatAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 20)
    private String dangerLevel; // HIGH / CRITICAL

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(nullable = false)
    private Double triggerLat;

    @Column(nullable = false)
    private Double triggerLng;

    @Column(nullable = false, length = 50)
    private String source; // DISASTER_MSG / WEATHER

    @Column(nullable = false, updatable = false)
    private LocalDateTime analyzedAt;

    @Builder
    public ThreatAnalysis(String dangerLevel, String keywords,
                          Double triggerLat, Double triggerLng, String source) {
        this.dangerLevel = dangerLevel;
        this.keywords = keywords;
        this.triggerLat = triggerLat;
        this.triggerLng = triggerLng;
        this.source = source;
        this.analyzedAt = LocalDateTime.now();
    }
}
