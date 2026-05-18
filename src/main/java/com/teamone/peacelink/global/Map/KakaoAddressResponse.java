package com.teamone.peacelink.global.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAddressResponse {

    private List<Document> documents;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private Address address;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        private String region_1depth_name; // 시도
        private String region_2depth_name; // 시군구
        private String region_3depth_name; // 동
    }
}