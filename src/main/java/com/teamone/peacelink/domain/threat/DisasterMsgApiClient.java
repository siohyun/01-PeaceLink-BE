package com.teamone.peacelink.domain.threat;

import com.teamone.peacelink.domain.threat.DTO.DisasterMsgApiResponse;
import com.teamone.peacelink.domain.threat.DTO.DisasterMsgItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DisasterMsgApiClient {

    private final RestClient restClient;

    @Value("${safetydata.service-key:}")
    private String serviceKey;

    private volatile List<DisasterMsgItem> cache = List.of();
    private volatile LocalDateTime cacheUpdatedAt = null;
    private static final int CACHE_MINUTES = 30;

    public List<DisasterMsgItem> fetchRecent(Double lat, Double lng) {
        if (isCacheValid()) return cache;

        try {
            String today = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String yesterday = LocalDate.now().minusDays(1)
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            List<DisasterMsgItem> result = fetch(today);

            if (result.isEmpty()) {
                log.info("오늘 재난문자 없음 — 어제 날짜로 재조회");
                result = fetch(yesterday);  // ✅ fetchWithoutDate() 대신 어제 날짜 지정
            }

            cache = result;
            cacheUpdatedAt = LocalDateTime.now();
            return result;

        } catch (Exception e) {
            log.warn("긴급재난문자 API 호출 실패 — 캐시 반환: {}", e.getMessage());
            return cache;
        }
    }

    // ✅ 날짜 지정 조회
    private List<DisasterMsgItem> fetch(String crtDt) {
        DisasterMsgApiResponse response = restClient.get()
                .uri(u -> u
                        .scheme("https")
                        .host("www.safetydata.go.kr")
                        .path("/V2/api/DSSP-IF-00247")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("returnType", "json")
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 100)
                        .queryParam("crtDt", crtDt)
                        .build())
                .retrieve()
                .body(DisasterMsgApiResponse.class);

        return parse(response);
    }

    // ✅ 날짜 없이 전체 최신 조회
    private List<DisasterMsgItem> fetchWithoutDate() {
        DisasterMsgApiResponse response = restClient.get()
                .uri(u -> u
                        .scheme("https")
                        .host("www.safetydata.go.kr")
                        .path("/V2/api/DSSP-IF-00247")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("returnType", "json")
                        .queryParam("pageNo", 1)
                        .queryParam("numOfRows", 100)
                        .build())
                .retrieve()
                .body(DisasterMsgApiResponse.class);

        return parse(response);
    }

    // ✅ 공통 파싱 로직
    private List<DisasterMsgItem> parse(DisasterMsgApiResponse response) {
        if (response == null || response.getBody() == null) return List.of();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        return response.getBody().stream()
                .filter(item -> {
                    String msg = item.getMsg();
                    if (msg == null) return false;
                    return !msg.contains("실종")
                            && !msg.contains("배회")
                            && !msg.contains("찾습니다");
                })
                // ✅ 최근 7일 이내 데이터만 허용
                .filter(item -> {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(item.getCreatedAt(), fmt);
                        return dt.isAfter(LocalDateTime.now().minusDays(7));
                    } catch (Exception e) {
                        return false;  // 날짜 파싱 실패하면 제외
                    }
                })
                .sorted((a, b) -> {
                    try {
                        return LocalDateTime.parse(b.getCreatedAt(), fmt)
                                .compareTo(LocalDateTime.parse(a.getCreatedAt(), fmt));
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .limit(20)
                .toList();
    }

    // ✅ 캐시 유효성 체크
    private boolean isCacheValid() {
        return cacheUpdatedAt != null
                && ChronoUnit.MINUTES.between(cacheUpdatedAt, LocalDateTime.now()) < CACHE_MINUTES;
    }
}