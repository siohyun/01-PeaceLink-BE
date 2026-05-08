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

    @Value("${public-data.service-key}")
    private String serviceKey;

    public List<DisasterMsgItem> fetchRecent() {
        try {
            DisasterMsgApiResponse response = restClient.get()
                    .uri(u -> u
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/1741000/DisasterMsg3/getDisasterMsg1List")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("pageNo", 1)
                            .queryParam("numOfRows", 100)
                            .queryParam("type", "json")
                            .build())
                    .retrieve()
                    .body(DisasterMsgApiResponse.class); // block() 제거, body()로 교체

            // ?. 대신 Java null 체크
            if (response == null
                    || response.getResponse() == null
                    || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null) {
                return List.of();
            }

            return response.getResponse().getBody().getItems().getItem();

        } catch (Exception e) {
            log.error("재난문자 API 호출 실패", e);
            return List.of();
        }
    }
}