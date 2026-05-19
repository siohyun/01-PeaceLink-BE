package com.teamone.peacelink.domain.evacuation.Entity;

import com.teamone.peacelink.domain.user.Entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evacuation_route")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EvacuationRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double originLat;

    @Column(nullable = false)
    private Double originLng;

    @Column(nullable = false)
    private Double destLat;

    @Column(nullable = false)
    private Double destLng;

    @Column(nullable = false)
    private Boolean isOffline;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String routeGeoJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public EvacuationRoute(User user, Double originLat, Double originLng,
                           Double destLat, Double destLng,
                           Boolean isOffline, String routeGeoJson) {
        this.user = user;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destLat = destLat;
        this.destLng = destLng;
        this.isOffline = isOffline;
        this.routeGeoJson = routeGeoJson;
        this.createdAt = LocalDateTime.now();
    }
}
