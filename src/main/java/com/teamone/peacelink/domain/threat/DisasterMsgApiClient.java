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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DisasterMsgApiClient {

    private final RestClient restClient;

    @Value("${safetydata.service-key:}")
    private String serviceKey;

    public List<DisasterMsgItem> fetchRecent() {
        try {
            // 오늘 날짜 기준으로 조회 (어제 포함하려면 어제 날짜도 추가 필요)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            DisasterMsgApiResponse response = restClient.get()
                    .uri(u -> u
                            .scheme("https")
                            .host("www.safetydata.go.kr")
                            .path("/V2/api/DSSP-IF-00247")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("returnType", "json")
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 100)
                            .queryParam("rgnNm", "서울특별시")
                            // crtDt 파라미터 제거 - 전체 조회 후 Java에서 필터링
                            .build())
                    .retrieve()
                    .body(DisasterMsgApiResponse.class);

            if (response == null || response.getBody() == null) return List.of();

            // ✅ 10시간으로 확장
            LocalDateTime cutoff = LocalDateTime.now().minusHours(10);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

            return response.getBody().stream()
                    .filter(item -> {
                        try {
                            LocalDateTime dt = LocalDateTime.parse(item.getCreatedAt(), fmt);
                            return dt.isAfter(cutoff);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();

        } catch (Exception e) {
            log.error("긴급재난문자 API 호출 실패", e);
            return List.of();
        }
    }
}