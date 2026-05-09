package com.teamone.peacelink.domain.threat.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisasterMsgItem {

    @JsonProperty("MSG_CN")
    private String msg;           // 재난문자 내용

    @JsonProperty("CRT_DT")
    private String createdAt;     // 생성일시 (yyyyMMddHHmmss)

    @JsonProperty("RCPTN_RGN_NM")
    private String areaName;      // 수신 지역명

    @JsonProperty("DST_SE_NM")
    private String disasterType;  // 재난유형

    @JsonProperty("EMRG_STEP_NM")
    private String emergencyStep; // 긴급단계

    // lat/lot 없음 → 지역명으로 좌표 변환 필요
}