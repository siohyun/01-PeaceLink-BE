package com.teamone.peacelink.domain.threat.DTO;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisasterMsgApiResponse {

    private Header header;

    private int numOfRows;
    private int pageNo;
    private int totalCount;         // ✅ body 밖으로 이동

    private List<DisasterMsgItem> body;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
        private String errorMsg;
    }
}