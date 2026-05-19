package com.teamone.peacelink.domain.report.Entity;

import com.teamone.peacelink.domain.user.Entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 50)
    private ReportType reportType;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(length = 200)
    private String description;

    // 위험도 판정 결과
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(columnDefinition = "TEXT")
    private String riskReason; // 위험도 판정 근거

    // Google Grounding 검증 결과
    @Column(nullable = false)
    private Boolean verified; // 검증된 정보 여부

    @Column(columnDefinition = "TEXT")
    private String verifiedSummary; // 검증된 요약 정보

    @Column(columnDefinition = "TEXT")
    private String groundingSources; // 출처 JSON

    // 미디어 파일 경로
    @ElementCollection
    @CollectionTable(name = "report_media", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "media_url")
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();

    @Column
    private String audioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;
}