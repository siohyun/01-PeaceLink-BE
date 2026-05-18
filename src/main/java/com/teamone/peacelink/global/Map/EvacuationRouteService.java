package com.teamone.peacelink.global.Map;

import com.teamone.peacelink.domain.evacuation.DTO.EvacuationRouteRequest;
import com.teamone.peacelink.domain.evacuation.DTO.EvacuationRouteResponse;
import com.teamone.peacelink.domain.evacuation.Entity.EvacuationRoute;
import com.teamone.peacelink.domain.evacuation.Repository.EvacuationRouteRepository;
import com.teamone.peacelink.domain.shelter.Entity.Shelter;
import com.teamone.peacelink.domain.shelter.Repository.ShelterRepository;
import com.teamone.peacelink.domain.user.Entity.User;
import com.teamone.peacelink.domain.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvacuationRouteService {

    private final EvacuationRouteRepository evacuationRouteRepository;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;
    private final KakaoMapClient kakaoMapClient;
    private final OsrmMapClient osrmMapClient;

    public EvacuationRouteResponse createRoute(EvacuationRouteRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Double originLat = request.getOriginLat() != null
                ? request.getOriginLat() : user.getLastLat();
        Double originLng = request.getOriginLng() != null
                ? request.getOriginLng() : user.getLastLng();

        if (originLat == null || originLng == null) {
            throw new IllegalStateException(
                    "현재 위치를 확인할 수 없습니다. 정확한 위치를 입력해주세요.");
        }

        Double destLat = request.getDestLat();
        Double destLng = request.getDestLng();
        Shelter targetShelter = null;

        if (destLat == null || destLng == null) {
            List<Shelter> shelters =
                    shelterRepository.findNearestShelters(originLat, originLng, 1);
            if (shelters.isEmpty()) {
                throw new IllegalStateException("주변에 운영 가능한 대피소가 없습니다.");
            }
            targetShelter = shelters.get(0);
            destLat = targetShelter.getLat();
            destLng = targetShelter.getLng();
        }

        // Kakao 실패 시 OSRM 자동 전환
        boolean isOffline = false;
        String routeGeoJson;
        try {
            routeGeoJson = kakaoMapClient.getRoute(originLat, originLng, destLat, destLng);
        } catch (Exception e) {
            log.warn("Kakao 경로 API 실패, OSRM으로 전환");
            isOffline = true;
            routeGeoJson = osrmMapClient.getRoute(originLat, originLng, destLat, destLng);
        }

        user.updateLocation(originLat, originLng);

        Double distanceKm = calculateDistance(originLat, originLng, destLat, destLng);

        EvacuationRoute route = EvacuationRoute.builder()
                .user(user)
                .originLat(originLat)
                .originLng(originLng)
                .destLat(destLat)
                .destLng(destLng)
                .isOffline(isOffline)
                .routeGeoJson(routeGeoJson)
                .build();

        return EvacuationRouteResponse.of(
                evacuationRouteRepository.save(route), targetShelter, distanceKm);
    }

    @Transactional(readOnly = true)
    public List<EvacuationRouteResponse> getRoutesByUser(java.util.UUID userId) {
        return evacuationRouteRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(r -> EvacuationRouteResponse.of(r, null, null))
                .toList();
    }

    private Double calculateDistance(Double lat1, Double lng1,
                                     Double lat2, Double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}