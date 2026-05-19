package com.teamone.peacelink.global.Map;

public interface MapApiClient {
    String getRoute(Double originLat, Double originLng,
                    Double destLat, Double destLng);
}