package com.teamone.peacelink.global.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.teamone.peacelink.global.Map.KakaoAddressResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoMapClient implements MapApiClient {

    private final RestClient restClient;

    @Value("${kakao.rest-api-key}")
    private String kakaoKey;

    @Override
    public String getRoute(Double originLat, Double originLng,
                           Double destLat, Double destLng) {
        return restClient.get()
                .uri(u -> u
                        .scheme("https")
                        .host("apis-navi.kakaomobility.com")
                        .path("/v1/directions")
                        .queryParam("origin", originLng + "," + originLat)
                        .queryParam("destination", destLng + "," + destLat)
                        .queryParam("priority", "RECOMMEND")
                        .build())
                .header("Authorization", "KakaoAK " + kakaoKey)
                .retrieve()
                .body(String.class);
    }

    public KakaoAddressResponse.Address reverseGeocode(Double lat, Double lng) {

        KakaoAddressResponse response = restClient.get()
                .uri(u -> u
                        .scheme("https")
                        .host("dapi.kakao.com")
                        .path("/v2/local/geo/coord2address.json")
                        .queryParam("x", lng)
                        .queryParam("y", lat)
                        .build())
                .header("Authorization", "KakaoAK " + kakaoKey)
                .retrieve()
                .body(KakaoAddressResponse.class);

        if (response == null ||
                response.getDocuments() == null ||
                response.getDocuments().isEmpty()) {
            return null;
        }

        return response.getDocuments().get(0).getAddress();
    }
}