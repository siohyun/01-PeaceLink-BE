package com.teamone.peacelink.domain.shelter.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "shelter")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    private Integer capacity;

    @Column(nullable = false)
    private Boolean isAvailable;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Shelter(String name, String code, Double lat, Double lng,
                   Integer capacity, Boolean isAvailable) {
        this.name = name;
        this.code = code;
        this.lat = lat;
        this.lng = lng;
        this.capacity = capacity;
        this.isAvailable = isAvailable;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, Double lat, Double lng, Integer capacity) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.capacity = capacity;
    }
}
