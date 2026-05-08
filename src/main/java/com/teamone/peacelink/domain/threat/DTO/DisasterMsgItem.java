package com.teamone.peacelink.domain.threat.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisasterMsgItem {

    @JsonProperty("msg")
    private String msg;         // 재난문자 내용

    @JsonProperty("crtDt")
    private String createdAt;   // 발송 시각

    @JsonProperty("mlsfrAreaNm")
    private String areaName;    // 발송 지역명

    @JsonProperty("lat")
    private String lat;         // 위도

    @JsonProperty("lot")        // 공공API는 lot으로 내려옴
    private String lng;         // 경도

    @JsonProperty("dsstrSeNm")
    private String disasterType; // 재난 유형 (지진, 태풍 등)
}