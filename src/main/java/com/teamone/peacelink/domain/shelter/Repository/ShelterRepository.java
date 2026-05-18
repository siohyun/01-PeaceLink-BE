package com.teamone.peacelink.domain.shelter.Repository;

import com.teamone.peacelink.domain.shelter.Entity.Shelter;
import org.hibernate.validator.constraints.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShelterRepository extends JpaRepository<Shelter, UUID> {

    Optional<Shelter> findByCode(String code);

    @Query(value = """
        SELECT *, (
            6371 * ACOS(
                COS(RADIANS(:lat)) * COS(RADIANS(lat)) *
                COS(RADIANS(lng) - RADIANS(:lng)) +
                SIN(RADIANS(:lat)) * SIN(RADIANS(lat))
            )
        ) AS distance
        FROM shelter
        WHERE is_available = true
        ORDER BY distance ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Shelter> findNearestShelters(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("limit") int limit
    );
}