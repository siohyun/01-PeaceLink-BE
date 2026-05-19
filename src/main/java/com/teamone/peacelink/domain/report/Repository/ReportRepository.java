package com.teamone.peacelink.domain.report.Repository;

import com.teamone.peacelink.domain.report.Entity.Report;
import com.teamone.peacelink.domain.report.Entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query(value = """
        SELECT * FROM reports r
        WHERE r.verified = true
          AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.lat))
               * cos(radians(r.lng) - radians(:lng))
               + sin(radians(:lat)) * sin(radians(r.lat)))) < :radiusKm
        ORDER BY r.created_at DESC
        """, nativeQuery = true)
    List<Report> findVerifiedReportsNearby(@Param("lat") Double lat,
                                           @Param("lng") Double lng,
                                           @Param("radiusKm") Double radiusKm);

    @Query("""
        SELECT r FROM Report r
        WHERE r.status = :status
          AND r.createdAt >= :since
        ORDER BY r.createdAt DESC
        """)
    List<Report> findByStatusAndCreatedAtAfter(
            @Param("status") ReportStatus status,
            @Param("since") LocalDateTime since);


    @Query(value = "SELECT * FROM reports WHERE user_id = UUID_TO_BIN(:userId)", nativeQuery = true)
    List<Report> findByUserIdBinary(@Param("userId") String userId);
}