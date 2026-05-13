package com.teamone.peacelink.domain.threat.Service;

import com.teamone.peacelink.domain.threat.DisasterMsgApiClient;
import com.teamone.peacelink.domain.threat.DTO.DisasterMsgItem;
import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import com.teamone.peacelink.domain.threat.Repository.ThreatAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThreatSyncService {

    private final DisasterMsgApiClient disasterMsgApiClient;
    private final ThreatAnalysisRepository threatAnalysisRepository;

    private static final Map<String, String> KEYWORD_DANGER_MAP = Map.of(
            "포격", "CRITICAL",
            "미사일", "CRITICAL",
            "폭발", "CRITICAL",
            "지진", "HIGH",
            "홍수", "HIGH",
            "태풍", "HIGH",
            "화재", "HIGH"
    );

    // 광역시도 기준 대표 좌표
    private static final Map<String, double[]> REGION_COORDS = Map.ofEntries(
            Map.entry("서울", new double[]{37.5665, 126.9780}),
            Map.entry("부산", new double[]{35.1796, 129.0756}),
            Map.entry("인천", new double[]{37.4563, 126.7052}),
            Map.entry("대구", new double[]{35.8714, 128.6014}),
            Map.entry("광주", new double[]{35.1595, 126.8526}),
            Map.entry("대전", new double[]{36.3504, 127.3845}),
            Map.entry("울산", new double[]{35.5384, 129.3114}),
            Map.entry("세종", new double[]{36.4801, 127.2890}),
            Map.entry("경기", new double[]{37.4138, 127.5183}),
            Map.entry("강원", new double[]{37.8228, 128.1555}),
            Map.entry("충북", new double[]{36.6357, 127.4913}),
            Map.entry("충남", new double[]{36.5184, 126.8000}),
            Map.entry("전북", new double[]{35.7175, 127.1530}),
            Map.entry("전남", new double[]{34.8679, 126.9910}),
            Map.entry("경북", new double[]{36.4919, 128.8889}),
            Map.entry("경남", new double[]{35.4606, 128.2132}),
            Map.entry("제주", new double[]{33.4890, 126.4983})
    );

    @Scheduled(fixedDelay = 300_000)
    public void syncScheduled() {
        log.info("재난문자 동기화 시작");
        sync();
    }

    @Transactional
    public void sync() {
        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent();

        for (DisasterMsgItem item : items) {
            String dangerLevel = classifyDangerLevel(item.getMsg());
            if (dangerLevel == null) continue;

            double[] coords = resolveCoords(item.getAreaName());
            if (coords == null) {
                log.warn("좌표 변환 실패 - 지역명: {}", item.getAreaName());
                continue;
            }

            threatAnalysisRepository.save(ThreatAnalysis.builder()
                    .dangerLevel(dangerLevel)
                    .keywords(item.getMsg())
                    .triggerLat(coords[0])
                    .triggerLng(coords[1])
                    .source("DISASTER_MSG")
                    .build());
        }
    }

    private double[] resolveCoords(String areaName) {
        if (areaName == null) return null;
        return REGION_COORDS.entrySet().stream()
                .filter(e -> areaName.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String classifyDangerLevel(String msg) {
        if (msg == null) return null;
        return KEYWORD_DANGER_MAP.entrySet().stream()
                .filter(e -> msg.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

}