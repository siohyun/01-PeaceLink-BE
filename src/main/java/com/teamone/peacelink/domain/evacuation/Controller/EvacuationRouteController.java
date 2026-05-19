package com.teamone.peacelink.domain.evacuation.Controller;

import com.teamone.peacelink.domain.evacuation.DTO.EvacuationRouteRequest;
import com.teamone.peacelink.domain.evacuation.DTO.EvacuationRouteResponse;
import com.teamone.peacelink.global.Map.EvacuationRouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evacuation")
@RequiredArgsConstructor
public class EvacuationRouteController {

    private final EvacuationRouteService evacuationRouteService;

    // 대피 경로 생성
    @PostMapping("/route")
    public ResponseEntity<EvacuationRouteResponse> createRoute(
            @Valid @RequestBody EvacuationRouteRequest request) {
        return ResponseEntity.ok(evacuationRouteService.createRoute(request));
    }

    // 사용자 대피 경로 이력 조회
    @GetMapping("/route")
    public ResponseEntity<List<EvacuationRouteResponse>> getRoutesByUser(
            @RequestParam UUID userId) {
        return ResponseEntity.ok(evacuationRouteService.getRoutesByUser(userId));
    }
}