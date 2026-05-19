package com.teamone.peacelink.domain.threat.DTO;

import com.teamone.peacelink.domain.report.Entity.Report;
import com.teamone.peacelink.domain.report.Entity.RiskLevel;
import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Getter
@Builder
public class SituationResponse {

    private String id;
    private String icon;
    private String title;
    private String location;
    private String level;
    private long minutesAgo;

    // 기존 메서드 유지 ──────────────────────────────────────────────────────

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

    // ✅ 신규 추가 ──────────────────────────────────────────────────────────

    public static SituationResponse fromReport(Report report) {
        return SituationResponse.builder()
                .id("REPORT-" + report.getId().toString().substring(0, 8))
                .icon(mapIconFromReportType(report.getReportType().getDisplayName()))
                .title(buildReportTitle(report))
                .location("시민제보")
                .level(mapLevelFromRisk(report.getRiskLevel()))
                .minutesAgo(ChronoUnit.MINUTES.between(
                        report.getCreatedAt(), LocalDateTime.now()))
                .build();
    }

    private static String buildReportTitle(Report report) {
        // "[시민제보] 화재·연기 - 설명 앞 20자…" 형태
        String base = "[시민제보] " + report.getReportType().getDisplayName();
        if (report.getDescription() != null && !report.getDescription().isBlank()) {
            String desc = report.getDescription().trim();
            String suffix = " - " + (desc.length() > 15
                    ? desc.substring(0, 15) + "…" : desc);
            return base + suffix;
        }
        return base;
    }

    private static String mapIconFromReportType(String displayName) {
        if (displayName == null) return "⚠️";
        return switch (displayName) {
            case "화재·연기"  -> "🔥";
            case "폭발·포격"  -> "💥";
            case "구조 요청"  -> "🆘";
            case "도로 통제"  -> "🚧";
            default           -> "⚠️";
        };
    }

    private static String mapLevelFromRisk(RiskLevel riskLevel) {
        if (riskLevel == null) return "안전";
        return switch (riskLevel) {
            case CRITICAL -> "높음";
            case HIGH     -> "중간";
            case MEDIUM   -> "낮음";
            default       -> "안전";
        };
    }

    // 기존 private 메서드 유지 ───────────────────────────────────────────────

    private static String buildTitle(DisasterMsgItem item) {
        String msg = item.getMsg();
        if (msg == null) return "재난 알림";
        String cleaned = msg.replaceAll("\\[.*?\\]\\s*", "").trim();
        return cleaned.length() > 20 ? cleaned.substring(0, 20) + "…" : cleaned;
    }

    private static long calcMinutesAgo(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) return 0;
        try {
            // ✅ 두 가지 포맷 모두 시도
            DateTimeFormatter fmt1 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            DateTimeFormatter fmt2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            LocalDateTime dt;
            try {
                dt = LocalDateTime.parse(createdAt, fmt1);
            } catch (Exception e) {
                dt = LocalDateTime.parse(createdAt, fmt2);
            }
            return Math.max(ChronoUnit.MINUTES.between(dt, LocalDateTime.now()), 0);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: '{}'", createdAt); // ✅ 실패 시 로그
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
            case "산불", "화재" -> "🔥";
            case "홍수", "호우" -> "🌊";
            case "지진"         -> "🌏";
            case "태풍"         -> "🌀";
            case "미세먼지"     -> "😷";
            case "교통통제", "교통" -> "🚧";
            default             -> "⚠️";
        };
    }

    private static String formatLocation(Double lat, Double lng) {
        if (lat == null || lng == null) return "위치 미상";
        return String.format("위도 %.4f / 경도 %.4f", lat, lng);
    }
}