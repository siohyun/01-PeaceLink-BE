package com.teamone.peacelink.domain.evacuation.DTO;

import com.teamone.peacelink.domain.evacuation.Entity.EvacuationRoute;
import com.teamone.peacelink.domain.shelter.Entity.Shelter;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EvacuationRouteResponse {

    private UUID id;
    private Double originLat;
    private Double originLng;
    private Double destLat;
    private Double destLng;
    private String shelterName;   // 목적지 대피소 이름
    private String shelterCode;   // 대피소 코드 (A-2)
    private Double distanceKm;    // 거리 (km)
    private Integer walkMinutes;  // 도보 시간 (분)
    private Boolean isOffline;    // 오프라인 경로 여부
    private String routeGeoJson;  // 경로 데이터
    private LocalDateTime createdAt;

    public static EvacuationRouteResponse of(EvacuationRoute route,
                                             Shelter shelter,
                                             Double distanceKm) {
        return EvacuationRouteResponse.builder()
                .id(route.getId())
                .originLat(route.getOriginLat())
                .originLng(route.getOriginLng())
                .destLat(route.getDestLat())
                .destLng(route.getDestLng())
                .shelterName(shelter != null ? shelter.getName() : null)
                .shelterCode(shelter != null ? shelter.getCode() : null)
                .distanceKm(distanceKm != null
                        ? Math.round(distanceKm * 10.0) / 10.0 : null)
                .walkMinutes(distanceKm != null
                        ? (int) Math.ceil(distanceKm / 4.0 * 60) : null)
                .isOffline(route.getIsOffline())
                .routeGeoJson(route.getRouteGeoJson())
                .createdAt(route.getCreatedAt())
                .build();
    }
}
