package com.teamone.peacelink.domain.shelter;

import com.teamone.peacelink.domain.shelter.DTO.ShelterApiResponse;
import com.teamone.peacelink.domain.shelter.DTO.ShelterItem;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShelterApiClient {

    private final RestClient restClient;

    @Value("${public-data.service-key}")
    private String serviceKey;

    @Value("${public-data.shelter.page-size}")
    private int pageSize;

    @Value("${public-data.shelter.url}")
    private String baseUrl;

    public List<ShelterItem> fetchAll() {
        List<ShelterItem> result = new ArrayList<>();
        int pageNo = 1;

        while (true) {
            ShelterApiResponse response = fetch(pageNo);

            if (response == null
                    || response.getResponse() == null
                    || response.getResponse().getBody() == null
                    || response.getResponse().getBody().getItems() == null) {
                break;
            }

            List<ShelterItem> items = response.getResponse().getBody().getItems().getItem();
            if (items == null || items.isEmpty()) break;

            result.addAll(items);

            if (result.size() >= response.getResponse().getBody().getTotalCount()) break;
            pageNo++;
        }

        log.info("대피소 API 조회 완료 - {}건", result.size());
        return result;
    }

    private ShelterApiResponse fetch(int pageNo) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(baseUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", pageSize)
                    .queryParam("returnType", "JSON")
                    .build(false)
                    .toUri();

            log.debug("대피소 API 요청 URL: {}", uri);
            log.info("대피소 API 요청 URL: {}", uri);

            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ShelterApiResponse.class);

        } catch (Exception e) {
            log.error("대피소 API 호출 실패 - pageNo: {}", pageNo, e);
            return null;
        }
    }
}