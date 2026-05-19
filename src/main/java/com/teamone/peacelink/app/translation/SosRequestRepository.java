package com.teamone.peacelink.app.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface SosRequestRepository extends JpaRepository<SosRequest, Long> {
    List<SosRequest> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<SosRequest> findBySituationType(String situationType);
}