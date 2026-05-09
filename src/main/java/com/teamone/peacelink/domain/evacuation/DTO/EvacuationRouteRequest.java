package com.teamone.peacelink.domain.evacuation.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class EvacuationRouteRequest {

    @NotNull
    private UUID userId;

    private Double originLat;
    private Double originLng;

    private Double destLat;   // null이면 가장 가까운 대피소 자동 설정
    private Double destLng;
}
