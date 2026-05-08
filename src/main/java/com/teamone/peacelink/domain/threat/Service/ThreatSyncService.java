package com.teamone.peacelink.domain.threat.Service;

import com.teamone.peacelink.domain.threat.DisasterMsgApiClient;
import com.teamone.peacelink.domain.threat.DTO.DisasterMsgItem;
import com.teamone.peacelink.domain.threat.Entity.ThreatAnalysis;
import com.teamone.peacelink.domain.threat.Repository.ThreatAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public void sync() {
        List<DisasterMsgItem> items = disasterMsgApiClient.fetchRecent();

        for (DisasterMsgItem item : items) {
            if (item.getLat() == null || item.getLng() == null) continue;

            String dangerLevel = classifyDangerLevel(item.getMsg());
            if (dangerLevel == null) continue;

            try {
                threatAnalysisRepository.save(ThreatAnalysis.builder()
                        .dangerLevel(dangerLevel)
                        .keywords(item.getMsg())
                        .triggerLat(Double.parseDouble(item.getLat()))
                        .triggerLng(Double.parseDouble(item.getLng()))
                        .source("DISASTER_MSG")
                        .build());
            } catch (NumberFormatException e) {
                log.warn("위협 데이터 파싱 실패: {}", item.getMsg());
            }
        }
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