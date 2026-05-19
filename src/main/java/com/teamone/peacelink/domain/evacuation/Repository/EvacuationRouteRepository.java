package com.teamone.peacelink.domain.evacuation.Repository;

import com.teamone.peacelink.domain.evacuation.Entity.EvacuationRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvacuationRouteRepository extends JpaRepository<EvacuationRoute, UUID> {
    List<EvacuationRoute> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
