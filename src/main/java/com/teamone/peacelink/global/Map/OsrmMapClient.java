package com.teamone.peacelink.global.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class OsrmMapClient implements MapApiClient {

    private final RestClient restClient;

    @Value("${osrm.url}")
    private String osrmUrl;

    @Override
    public String getRoute(Double originLat, Double originLng,
                           Double destLat, Double destLng) {
        String coordinates = originLng + "," + originLat
                + ";" + destLng + "," + destLat;

        return restClient.get()
                .uri(osrmUrl + "/" + coordinates + "?overview=full&geometries=geojson")
                .retrieve()
                .body(String.class);
    }
}