package com.teamone.peacelink.app.translation;

import com.teamone.peacelink.app.service.TranslationService;
import com.teamone.peacelink.global.Exception.NoiseTooLoudException;
import com.teamone.peacelink.global.Exception.STTFailException;
import com.teamone.peacelink.global.Exception.UnsupportedLanguageException;
import lombok.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    /**
     * COMM-01: 오프라인 특수 번역
     */
    @PostMapping("/offline-special")
    public ResponseEntity<?> offlineSpecialTranslate(@RequestBody OfflineTranslateRequest request) {
        try {
            String result = translationService.specialTermTranslate(
                    request.getText(),
                    request.getSourceLang(),
                    request.getTargetLang(),
                    request.getDomain()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "original", request.getText(),
                    "translated", result,
                    "domain", request.getDomain()
            ));
        } catch (UnsupportedLanguageException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "fallback", "symbol_list",
                    "symbols", translationService.getSymbolCommunicationList(request.getDomain()),
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * COMM-02: 온디바이스 양방향 통역 (오디오)
     */
    @PostMapping(value = "/interpret", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> interpret(
            @RequestPart("audio") MultipartFile audioFile,
            @RequestPart("sourceLang") String sourceLang,
            @RequestPart("targetLang") String targetLang
    ) {
        try {
            InterpretResult result = translationService.interpretAudio(
                    audioFile, sourceLang, targetLang);
            return ResponseEntity.ok(result);
        } catch (NoiseTooLoudException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "errorType", "NOISE_TOO_LOUD",
                    "message", "주변이 너무 시끄럽습니다. 조용한 곳에서 다시 시도하거나 필담 모드를 사용하세요.",
                    "fallbackMode", "TEXT_INPUT"
            ));
        } catch (STTFailException e) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "errorType", "STT_FAIL",
                    "message", "음성 인식에 실패했습니다. 다시 말씀해 주세요.",
                    "retry", true
            ));
        }
    }

    /**
     * COMM-02: 텍스트 필담 모드 폴백
     */
    @PostMapping("/interpret/text")
    public ResponseEntity<?> interpretText(@RequestBody TextInterpretRequest request) {
        String translated = translationService.specialTermTranslate(
                request.getText(),
                request.getSourceLang(),
                request.getTargetLang(),
                "general"
        );
        byte[] ttsAudio = translationService.textToSpeech(translated, request.getTargetLang());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "original", request.getText(),
                "translated", translated,
                "hasAudio", ttsAudio != null && ttsAudio.length > 0
        ));
    }

    /**
     * SOS-01: 다국어 디지털 구조 요청
     */
    @PostMapping("/sos")
    public ResponseEntity<SosResponse> sendSosRequest(@RequestBody SosRequestDto dto) {
        SosResponse response = translationService.processSosRequest(dto);
        return ResponseEntity.ok(response);
    }

//    /**
//     * SOS-01: 구조 요청 이력 조회
//     */
//    @GetMapping("/sos/history/{userId}")
//    public ResponseEntity<?> getSosHistory(@PathVariable String userId) {
//        return ResponseEntity.ok(translationService.getSosHistory(userId));
//    }

    // ===== DTO 클래스들 =====

    @Data
    public static class OfflineTranslateRequest {
        private String text;
        private String sourceLang;
        private String targetLang;
        private String domain;
    }

    @Data
    public static class TextInterpretRequest {
        private String text;
        private String sourceLang;
        private String targetLang;
    }

    @Data
    public static class SosRequestDto {
        private String userId;
        private String situationType;
        private String message;
        private String targetLanguage;
        private String outputType;
        private double latitude;
        private double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterpretResult {
        private boolean success;
        private String recognizedText;
        private String translatedText;
        private byte[] audioData;
        private String sttMethod;
    }
}