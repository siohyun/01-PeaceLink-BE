package com.teamone.peacelink.domain.user.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;


import java.time.LocalDateTime;



@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = true, length = 100)
    private String name;

    @Column(nullable = false, length = 10)
    private String language;

    private Double lastLat;
    private Double lastLng;
    private LocalDateTime lastSeen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String deviceId, String name, String language) {
        this.deviceId = deviceId;
        this.name = name;
        this.language = language != null ? language : "ko";
        this.createdAt = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }

    public void updateLocation(Double lat, Double lng) {
        this.lastLat = lat;
        this.lastLng = lng;
        this.lastSeen = LocalDateTime.now();
    }

    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }
}