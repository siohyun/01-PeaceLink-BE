package com.teamone.peacelink.domain.threat.DTO;

import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ThreatMarkerResponse {
    private UUID id;
    private Double lat;
    private Double lng;
    private String dangerLevel;
    private String keywords;
    private Double distanceKm;
    private LocalDateTime analyzedAt;

    public static ThreatMarkerResponse of(ThreatAnalysis t, Double distanceKm) {
        return ThreatMarkerResponse.builder()
                .id(t.getId())
                .lat(t.getTriggerLat())
                .lng(t.getTriggerLng())
                .dangerLevel(t.getDangerLevel())
                .keywords(t.getKeywords())
                .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                .analyzedAt(t.getAnalyzedAt())
                .build();
    }
}
