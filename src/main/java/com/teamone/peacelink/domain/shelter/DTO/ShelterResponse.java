package com.teamone.peacelink.domain.shelter.DTO;

import com.teamone.peacelink.domain.shelter.Entity.Shelter;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class ShelterResponse {
    private UUID id;
    private String name;
    private String code;
    private Double lat;
    private Double lng;
    private Double distanceKm;
    private Integer walkMinutes;
    private Boolean isAvailable;

    public static ShelterResponse of(Shelter shelter, Double distanceKm) {
        return ShelterResponse.builder()
                .id(shelter.getId())
                .name(shelter.getName())
                .code(shelter.getCode())
                .lat(shelter.getLat())
                .lng(shelter.getLng())
                .distanceKm(Math.round(distanceKm * 10.0) / 10.0)
                .walkMinutes((int) Math.ceil(distanceKm / 4.0 * 60))
                .isAvailable(shelter.getIsAvailable())
                .build();
    }
}