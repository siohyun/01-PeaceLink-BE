package com.teamone.peacelink.domain.threat.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisasterMsgItem {

    @JsonProperty("SN")
    private Long sn;   // String → Long

    @JsonProperty("MSG_CN")
    private String msg;           // 재난문자 내용

    @JsonProperty("CRT_DT")
    private String createdAt;     // 생성일시

    @JsonProperty("RCPTN_RGN_NM")
    private String areaName;      // 수신 지역명

    @JsonProperty("DST_SE_NM")
    private String disasterType;  // 재해구분명

    @JsonProperty("EMRG_STEP_NM")
    private String emergencyStep; // 긴급단계명

    @JsonProperty("REG_YMD")
    private String regDate;       // ✅ 등록일자 (추가)

    @JsonProperty("MDFCN_YMD")
    private String modifiedDate;  // ✅ 수정일자 (추가)
}