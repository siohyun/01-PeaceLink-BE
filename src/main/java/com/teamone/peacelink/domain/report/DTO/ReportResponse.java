package com.teamone.peacelink.domain.report.DTO;

import com.teamone.peacelink.domain.report.Entity.Report;
import com.teamone.peacelink.domain.report.Entity.ReportStatus;
import com.teamone.peacelink.domain.report.Entity.ReportType;
import com.teamone.peacelink.domain.report.Entity.RiskLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ReportResponse {

    private UUID id;
    private UUID userId;
    private ReportType reportType;
    private Double lat;
    private Double lng;
    private String description;

    // 위험도 분석 결과
    private RiskLevel riskLevel;
    private String riskReason;

    // 검증 결과
    private Boolean verified;
    private String verifiedSummary;

    private List<String> mediaUrls;
    private ReportStatus status;
    private LocalDateTime createdAt;

    public static ReportResponse of(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .userId(report.getUser().getId())
                .reportType(report.getReportType())
                .lat(report.getLat())
                .lng(report.getLng())
                .description(report.getDescription())
                .riskLevel(report.getRiskLevel())
                .riskReason(report.getRiskReason())
                .verified(report.getVerified())
                .verifiedSummary(report.getVerifiedSummary())
                .mediaUrls(report.getMediaUrls())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}