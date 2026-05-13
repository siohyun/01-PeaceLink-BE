package com.teamone.peacelink.domain.threat.Controller;

import com.teamone.peacelink.domain.threat.DTO.DisasterMsgItem;
import com.teamone.peacelink.domain.threat.DTO.EmergencyAlertResponse;
import com.teamone.peacelink.domain.threat.DTO.SituationResponse;
import com.teamone.peacelink.domain.threat.DTO.ThreatMarkerResponse;
import com.teamone.peacelink.domain.threat.DisasterMsgApiClient;
import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import com.teamone.peacelink.domain.threat.Repository.ThreatAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/evacuation")
@RequiredArgsConstructor
public class ThreatController {

    private final ThreatAnalysisRepository threatAnalysisRepository;
    private final DisasterMsgApiClient disasterMsgApiClient;

    @GetMapping("/threats/nearby")
    public ResponseEntity<List<ThreatMarkerResponse>> getNearbyThreats(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        LocalDateTime since = LocalDateTime.now().minusHours(2);
        List<ThreatAnalysis> threats =
                threatAnalysisRepository.findNearbyThreats(lat, lng, 5.0, since);

        List<ThreatMarkerResponse> result = threats.stream()
                .map(t -> {
                    Double dist = calculateDistance(
                            lat, lng, t.getTriggerLat(), t.getTriggerLng());
                    return ThreatMarkerResponse.of(t, dist);
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    @GetMapping("/threats/alerts/latest")
    public ResponseEntity<EmergencyAlertResponse> getLatestAlert(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent();
        if (items.isEmpty()) return ResponseEntity.noContent().build();

        DisasterMsgItem latest = items.get(0);
        return ResponseEntity.ok(EmergencyAlertResponse.fromDisasterMsg(latest));
    }

    // 기존 /threats/alerts 도 교체
    @GetMapping("/threats/alerts")
    public ResponseEntity<List<EmergencyAlertResponse>> getAlerts(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent();

        List<EmergencyAlertResponse> alerts = items.stream()
                .map(EmergencyAlertResponse::fromDisasterMsg)
                .toList();

        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/threats/situations")
    public ResponseEntity<List<SituationResponse>> getSituations(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent();

        List<SituationResponse> result = items.stream()
                .map(SituationResponse::fromDisasterMsg)
                .toList();

        return ResponseEntity.ok(result);
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
