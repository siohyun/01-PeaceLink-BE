package com.teamone.peacelink.domain.threat.Repository;

import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ThreatAnalysisRepository extends JpaRepository<ThreatAnalysis, UUID> {

    @Query(value = """
        SELECT *, (
            6371 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(trigger_lat)) *
                COS(RADIANS(trigger_lng) - RADIANS(:lng)) +
                SIN(RADIANS(:lat)) * SIN(RADIANS(trigger_lat))
            )
        ) AS distance
        FROM threat_analysis
        WHERE analyzed_at >= :since
        HAVING distance <= :radiusKm
        ORDER BY distance ASC
        """, nativeQuery = true)
    List<ThreatAnalysis> findNearbyThreats(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm,
            @Param("since") LocalDateTime since
    );

    @Query(value = """
        SELECT COUNT(*) FROM (
            SELECT id, (
                6371 * ACOS(
                    COS(RADIANS(:lat)) * COS(RADIANS(trigger_lat)) *
                    COS(RADIANS(trigger_lng) - RADIANS(:lng)) +
                    SIN(RADIANS(:lat)) * SIN(RADIANS(trigger_lat))
                )
            ) AS distance
            FROM threat_analysis
            HAVING distance <= :radiusKm
        ) AS nearby
        """, nativeQuery = true)
    long countByLocationNear(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radiusKm") Double radiusKm
    );
}
