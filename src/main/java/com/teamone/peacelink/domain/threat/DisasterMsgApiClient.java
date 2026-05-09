package com.teamone.peacelink.domain.threat;

import com.teamone.peacelink.domain.threat.DTO.DisasterMsgApiResponse;
import com.teamone.peacelink.domain.threat.DTO.DisasterMsgItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
            DisasterMsgApiResponse response = restClient.get()
                    .uri(u -> u
                            .scheme("https")
                            .host("www.safetydata.go.kr")
                            .path("/V2/api/DSSP-IF-10941")  // 신규 엔드포인트
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("returnType", "json")
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 100)
                            .queryParam("rgnNm", "서울특별시")
                            .build())
                    .retrieve()
                    .body(DisasterMsgApiResponse.class);

            if (response == null || response.getBody() == null) {
                return List.of();
            }
            return response.getBody();

        } catch (Exception e) {
            log.error("긴급재난문자 API 호출 실패", e);
            return List.of();
        }
    }
}