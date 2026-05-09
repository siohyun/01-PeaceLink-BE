package com.teamone.peacelink.domain.report.Repository;

import com.teamone.peacelink.domain.report.Entity.Report;
import com.teamone.peacelink.domain.report.Entity.ReportStatus;
import com.teamone.peacelink.domain.report.Entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    // 반경 내 검증된 제보 조회 (위험 지도 표시용)
    @Query(value = """
            SELECT * FROM reports r
            WHERE r.verified = true
              AND r.status != 'REJECTED'
              AND (6371 * acos(
                    cos(radians(:lat)) * cos(radians(r.lat))
                    * cos(radians(r.lng) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(r.lat))
                  )) < :radiusKm
            ORDER BY r.created_at DESC
            """, nativeQuery = true)
    List<Report> findVerifiedReportsNearby(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm);

    List<Report> findByReportTypeAndStatusOrderByCreatedAtDesc(
            ReportType reportType, ReportStatus status);
}