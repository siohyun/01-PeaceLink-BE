package com.teamone.peacelink.domain.threat.DTO;

import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
public class SituationResponse {

    private String id;
    private String icon;
    private String title;
    private String location;
    private String level;       // "높음" | "중간" | "낮음" | "안전"
    private long minutesAgo;

    public static SituationResponse fromDisasterMsg(DisasterMsgItem item) {
        long minutesAgo = calcMinutesAgo(item.getCreatedAt());

        return SituationResponse.builder()
                .id(String.valueOf(item.getSn()))
                .icon(mapIcon(item.getDisasterType()))
                .title(buildTitle(item))
                .location(item.getAreaName() != null
                        ? item.getAreaName().trim() : "위치 미상")
                .level(mapLevel(item.getEmergencyStep()))
                .minutesAgo(minutesAgo)
                .build();
    }

    private static String buildTitle(DisasterMsgItem item) {
        // MSG_CN 앞 20자만 잘라서 제목으로 사용
        String msg = item.getMsg();
        if (msg == null) return "재난 알림";
        // 대괄호 태그 제거: "[서울경찰청] 노원구..." → "노원구..."
        String cleaned = msg.replaceAll("\\[.*?\\]\\s*", "").trim();
        return cleaned.length() > 20 ? cleaned.substring(0, 20) + "…" : cleaned;
    }

    private static long calcMinutesAgo(String createdAt) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime dt = LocalDateTime.parse(createdAt, fmt);
            return Math.max(ChronoUnit.MINUTES.between(dt, LocalDateTime.now()), 0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static String mapLevel(String emergencyStep) {
        if (emergencyStep == null) return "안전";
        return switch (emergencyStep) {
            case "위급재난" -> "높음";
            case "긴급재난" -> "중간";
            case "안전안내" -> "낮음";
            default -> "안전";
        };
    }

    private static String mapIcon(String disasterType) {
        if (disasterType == null) return "⚠️";
        return switch (disasterType) {
            case "산불" -> "🔥";
            case "화재" -> "🔥";
            case "홍수", "호우" -> "🌊";
            case "지진" -> "🌏";
            case "태풍" -> "🌀";
            case "미세먼지" -> "😷";
            case "교통통제", "교통" -> "🚧";
            case "기타" -> "📢";
            default -> "⚠️";
        };
    }

    // 좌표 → 위치 텍스트 (추후 역지오코딩으로 교체 가능)
    private static String formatLocation(Double lat, Double lng) {
        if (lat == null || lng == null) return "위치 미상";
        return String.format("위도 %.4f / 경도 %.4f", lat, lng);
    }
}