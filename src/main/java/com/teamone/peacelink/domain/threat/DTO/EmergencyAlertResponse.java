package com.teamone.peacelink.domain.threat.DTO;

import com.teamone.peacelink.domain.report.Entity.Report;
import com.teamone.peacelink.domain.report.Entity.ReportType;
import com.teamone.peacelink.domain.report.Entity.RiskLevel;
import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class EmergencyAlertResponse {

    private String id;
    private String level;
    private String message;
    private String districtCode;

    // 기존 메서드 유지 ──────────────────────────────────────────────────────

    public static EmergencyAlertResponse from(ThreatAnalysis t) {
        return EmergencyAlertResponse.builder()
                .id(t.getId().toString())
                .level(mapLevel(t.getDangerLevel()))
                .message(t.getKeywords())
                .build();
    }

    public static EmergencyAlertResponse fromDisasterMsg(DisasterMsgItem item) {
        return EmergencyAlertResponse.builder()
                .id(String.valueOf(item.getSn()))
                .level(mapLevelFromStep(item.getEmergencyStep()))
                .message(item.getMsg())
                .build();
    }

    // ✅ 신규 추가 ──────────────────────────────────────────────────────────

    public static EmergencyAlertResponse fromReport(Report report) {
        return EmergencyAlertResponse.builder()
                .id("REPORT-" + report.getId().toString().substring(0, 8))
                .level(mapLevelFromRisk(report.getRiskLevel()))
                .message(buildMsg(report))
                .build();
    }

    private static String buildMsg(Report report) {
        StringBuilder sb = new StringBuilder();
        sb.append("[시민제보] ").append(report.getReportType().getDisplayName());

        if (report.getDescription() != null && !report.getDescription().isBlank()) {
            String desc = report.getDescription();
            sb.append(" - ").append(desc, 0, Math.min(desc.length(), 80));
        }
        if (report.getRiskLevel() != null) {
            sb.append(" (위험도: ").append(report.getRiskLevel().name()).append(")");
        }
        if (report.getVerifiedSummary() != null && !report.getVerifiedSummary().isBlank()) {
            String summary = report.getVerifiedSummary();
            sb.append(" / ").append(summary, 0, Math.min(summary.length(), 100));
        }
        return sb.toString();
    }

    // RiskLevel → 프론트 level
    private static String mapLevelFromRisk(RiskLevel riskLevel) {
        if (riskLevel == null) return "info";
        return switch (riskLevel) {
            case CRITICAL -> "critical";
            case HIGH     -> "warning";
            default       -> "info";
        };
    }

    // 기존 private 메서드 유지 ───────────────────────────────────────────────

    private static String mapLevel(String dangerLevel) {
        if (dangerLevel == null) return "info";
        return switch (dangerLevel.toUpperCase()) {
            case "HIGH", "위급재난" -> "critical";
            case "MEDIUM", "긴급재난" -> "warning";
            default -> "info";
        };
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