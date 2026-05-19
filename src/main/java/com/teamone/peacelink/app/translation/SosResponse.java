package com.teamone.peacelink.app.translation;

import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SosResponse {

    private Long requestId;
    private String originalMessage;
    private String translatedMessage;
    private String targetLanguage;
    private String outputType;
    private byte[] audioData;
    private String displayText;
    private boolean success;
    private String errorMessage;
    private String situationType;
    private String timestamp;

    public static SosResponse error(String message) {
        return SosResponse.builder()
                .success(false)
                .errorMessage(message)
                .build();
    }
}