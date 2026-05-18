package com.teamone.peacelink.domain.threat.Controller;

import com.teamone.peacelink.domain.report.Entity.ReportStatus;
import com.teamone.peacelink.domain.report.Repository.ReportRepository;
import com.teamone.peacelink.domain.threat.DTO.DisasterMsgItem;
import com.teamone.peacelink.domain.threat.DTO.EmergencyAlertResponse;
import com.teamone.peacelink.domain.threat.DTO.SituationResponse;
import com.teamone.peacelink.domain.threat.DTO.ThreatMarkerResponse;
import com.teamone.peacelink.domain.threat.DisasterMsgApiClient;
import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import com.teamone.peacelink.domain.threat.Repository.ThreatAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.teamone.peacelink.global.Map.KakaoMapClient;
import com.teamone.peacelink.global.Map.KakaoAddressResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/evacuation")
@RequiredArgsConstructor
public class ThreatController {

    private final ThreatAnalysisRepository threatAnalysisRepository;
    private final DisasterMsgApiClient disasterMsgApiClient;
    private final KakaoMapClient kakaoMapClient;
    private final ReportRepository reportRepository;

    private static final int REPORT_MERGE_HOURS = 6;

    // ── 공개 엔드포인트 ────────────────────────────────────────────────────

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

        // 최신 승인 제보가 있으면 우선 반환
        Optional<EmergencyAlertResponse> reportAlert = reportRepository
                .findByStatusAndCreatedAtAfter(
                        ReportStatus.VERIFIED,
                        LocalDateTime.now().minusHours(REPORT_MERGE_HOURS))
                .stream()
                .findFirst()
                .map(EmergencyAlertResponse::fromReport);

        if (reportAlert.isPresent()) return ResponseEntity.ok(reportAlert.get());

        // 없으면 공공 API
        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent(lat, lng);
        if (items.isEmpty()) return ResponseEntity.noContent().build();

        List<DisasterMsgItem> nearby = filterByLocation(items, lat, lng);
        DisasterMsgItem latest = nearby.isEmpty() ? items.get(0) : nearby.get(0);
        return ResponseEntity.ok(EmergencyAlertResponse.fromDisasterMsg(latest));
    }

    @GetMapping("/threats/alerts")
    public ResponseEntity<List<EmergencyAlertResponse>> getAlerts(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        // 공공 API 알림
        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent(lat, lng);
        items = filterByLocation(items, lat, lng);

        List<EmergencyAlertResponse> result = new ArrayList<>(
                items.stream().map(EmergencyAlertResponse::fromDisasterMsg).toList());

        // 승인된 시민 제보를 앞에 병합
        reportRepository
                .findByStatusAndCreatedAtAfter(
                        ReportStatus.VERIFIED,
                        LocalDateTime.now().minusHours(REPORT_MERGE_HOURS))
                .stream()
                .map(EmergencyAlertResponse::fromReport)
                .forEach(r -> result.add(0, r));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/threats/situations")
    public ResponseEntity<List<SituationResponse>> getSituations(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent(lat, lng);
        List<DisasterMsgItem> filtered = filterByLocation(items, lat, lng);
        List<DisasterMsgItem> toUse = filtered.size() >= 3 ? filtered : items;

        List<SituationResponse> result = new ArrayList<>(
                toUse.stream().limit(10).map(SituationResponse::fromDisasterMsg).toList());

        // 승인된 시민 제보를 앞에 병합
        reportRepository
                .findByStatusAndCreatedAtAfter(
                        ReportStatus.VERIFIED,
                        LocalDateTime.now().minusHours(REPORT_MERGE_HOURS))
                .stream()
                .map(SituationResponse::fromReport)
                .forEach(r -> result.add(0, r));

        return ResponseEntity.ok(result);
    }

    // ── private 유틸 메서드 ───────────────────────────────────────────────

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

    private List<DisasterMsgItem> filterByLocation(
            List<DisasterMsgItem> items,
            Double lat,
            Double lng) {

        KakaoAddressResponse.Address address =
                kakaoMapClient.reverseGeocode(lat, lng);

        if (address == null) return items;

        String city     = address.getRegion_2depth_name();
        String district = address.getRegion_3depth_name();

        return items.stream()
                .filter(item -> {
                    String area = item.getAreaName();
                    if (area == null) return false;
                    return area.contains(city) || area.contains(district);
                })
                .toList();
    }
}