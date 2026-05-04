package com.teamone.peacelink.app.translation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SosRequestRepository extends JpaRepository<SosRequest, Long> {}
