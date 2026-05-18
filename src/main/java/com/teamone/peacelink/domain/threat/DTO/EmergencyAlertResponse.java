package com.teamone.peacelink.domain.threat.DTO;

import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class EmergencyAlertResponse {

    private String id;
    private String level;    // "critical" | "warning" | "info"
    private String message;
    private String districtCode;

    public static EmergencyAlertResponse from(ThreatAnalysis t) {
        return EmergencyAlertResponse.builder()
                .id(t.getId().toString())
                .level(mapLevel(t.getDangerLevel()))
                .message(t.getKeywords())
                .build();
    }

    // 백엔드 dangerLevel → 프론트 level 변환
    private static String mapLevel(String dangerLevel) {
        if (dangerLevel == null) return "info";
        return switch (dangerLevel.toUpperCase()) {
            case "HIGH", "위급재난" -> "critical";
            case "MEDIUM", "긴급재난" -> "warning";
            default -> "info";
        };
    }

    public static EmergencyAlertResponse fromDisasterMsg(DisasterMsgItem item) {
        return EmergencyAlertResponse.builder()
                .id(String.valueOf(item.getSn()))
                .level(mapLevelFromStep(item.getEmergencyStep()))
                .message(item.getMsg())
                .build();
    }

    private static String mapLevelFromStep(String step) {
        if (step == null) return "info";
        return switch (step) {
            case "위급재난" -> "critical";
            case "긴급재난" -> "warning";
            default -> "info";
        };
    }
}