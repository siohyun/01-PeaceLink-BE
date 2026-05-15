package com.teamone.peacelink.app.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SosRequestRepository extends JpaRepository<SosRequest, Long> {
    List<SosRequest> findByUserIdOrderByCreatedAtDesc(String userId);
    List<SosRequest> findBySituationType(String situationType);
}