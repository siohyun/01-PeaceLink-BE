package com.teamone.peacelink.domain.shelter.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShelterItem {

    @JsonProperty("FCLT_NM")
    private String name;           // 시설명

    @JsonProperty("MNG_NO")
    private String code;           // 관리번호 (고유값)

    @JsonProperty("ROAD_NM_WHOL_ADDR")
    private String address;        // 도로명 주소

    @JsonProperty("LAT_EPSG4326")
    private String lat;            // 위도 (WGS84)

    @JsonProperty("LOT_EPST4326")
    private String lng;            // 경도 (WGS84)

    @JsonProperty("MAX_ACTC_PERNE")
    private String capacity;       // 최대 수용인원

    @JsonProperty("OPER_STTS")
    private String operStatus;     // 운영상태
}