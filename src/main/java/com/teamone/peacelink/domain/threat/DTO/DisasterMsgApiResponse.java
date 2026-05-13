package com.teamone.peacelink.domain.threat.DTO;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisasterMsgApiResponse {

    private Header header;          // ✅ Lombok @Getter로 getHeader() 자동 생성됨
    private List<DisasterMsgItem> body;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
        private int totalCount;
    }
}