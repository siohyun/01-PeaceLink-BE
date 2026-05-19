package com.teamone.peacelink.app.service;

import com.teamone.peacelink.app.translation.*;
import com.teamone.peacelink.global.Exception.NoiseTooLoudException;
import com.teamone.peacelink.global.Exception.STTFailException;
import com.teamone.peacelink.global.Exception.UnsupportedLanguageException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {

    @Value("${huggingface.api.token:mock_token}")
    private String hfToken;

    @Value("${huggingface.model.id:seohyun01/peacelink-gemma-medical}")
    private String gemmaModelId;

    @Value("${stt.noise.threshold:0.4}")
    private double noiseThreshold;

    @Value("${stt.whisper.api.url:https://api-inference.huggingface.co/models/openai/whisper-large-v3}")
    private String whisperApiUrl;

    private final RestTemplate restTemplate;
    private final SosRequestRepository sosRequestRepository;
    private final ObjectMapper objectMapper;

    // ============================================================
    // COMM-01: 오프라인 특수 번역
    // ============================================================

    public String specialTermTranslate(String text, String sourceLang,
                                       String targetLang, String domain) {
        if (!isSupportedLanguage(targetLang)) {
            throw new UnsupportedLanguageException("지원하지 않는 언어입니다: " + targetLang);
        }
        String domainContext = getDomainContext(domain);
        String prompt = buildTranslationPrompt(text, sourceLang, targetLang, domainContext);
        return callGemmaModel(prompt);
    }

    private String buildTranslationPrompt(String text, String src, String tgt, String context) {
        return String.format(
                "[INST] You are a specialized translator for %s terminology. " +
                        "Translate the following text from %s to %s accurately, " +
                        "preserving technical terms. Context: %s\n\nText: %s\n\n" +
                        "Provide only the translation, no explanation. [/INST]",
                context, src, tgt, context, text
        );
    }

    private String getDomainContext(String domain) {
        return switch (domain.toLowerCase()) {
            case "disaster" -> "disaster relief and emergency response";
            case "medical"  -> "medical and healthcare emergency";
            case "military" -> "military and defense operations";
            default         -> "general communication";
        };
    }

    public List<Map<String, String>> getSymbolCommunicationList(String domain) {
        return List.of(
                Map.of("symbol", "🆘", "meaning_ko", "도움 요청",  "meaning_en", "Need Help"),
                Map.of("symbol", "🏥", "meaning_ko", "의료 도움",  "meaning_en", "Medical Help"),
                Map.of("symbol", "🔥", "meaning_ko", "화재",       "meaning_en", "Fire"),
                Map.of("symbol", "💧", "meaning_ko", "물/홍수",    "meaning_en", "Flood"),
                Map.of("symbol", "🏠", "meaning_ko", "대피소",     "meaning_en", "Shelter"),
                Map.of("symbol", "🍞", "meaning_ko", "식량 필요",  "meaning_en", "Need Food"),
                Map.of("symbol", "💊", "meaning_ko", "약품 필요",  "meaning_en", "Need Medicine"),
                Map.of("symbol", "👨‍👩‍👧", "meaning_ko", "가족 찾기", "meaning_en", "Find Family")
        );
    }

    // ============================================================
    // COMM-02: 온디바이스 양방향 통역
    // ============================================================

    public TranslationController.InterpretResult interpretAudio(
            MultipartFile audioFile, String sourceLang, String targetLang) {

        byte[] audioBytes;
        try {
            audioBytes = audioFile.getBytes();
        } catch (Exception e) {
            throw new STTFailException("오디오 파일 읽기 실패");
        }

        // 노이즈 레벨 분석
        double noiseLevel = analyzeNoiseLevel(audioBytes);
        log.info("노이즈 레벨: {}", noiseLevel);

        if (noiseLevel > 0.90) {
            throw new NoiseTooLoudException("주변 소음이 너무 심합니다");
        }

        // STT 수행
        String recognizedText;
        String sttMethod;

        if (noiseLevel < noiseThreshold) {
            // 기기 내장 STT (클라이언트에서 수행 후 텍스트 전달 방식 권장)
            // 서버 수신 오디오는 Whisper로 처리
            recognizedText = performWhisperSTT(audioBytes, sourceLang);
            sttMethod = "device";
        } else {
            // 노이즈 중간 → Whisper API
            log.info("노이즈 수준이 높아 Whisper API로 전환");
            recognizedText = performWhisperSTT(audioBytes, sourceLang);
            sttMethod = "whisper_api";
        }

        if (recognizedText == null || recognizedText.isBlank()) {
            throw new STTFailException("음성 인식 결과가 없습니다");
        }

        // 번역
        String translated = specialTermTranslate(
                recognizedText, sourceLang, targetLang, "disaster");

        // TTS
        byte[] ttsAudio = textToSpeech(translated, targetLang);

        return TranslationController.InterpretResult.builder()
                .success(true)
                .recognizedText(recognizedText)
                .translatedText(translated)
                .audioData(ttsAudio)
                .sttMethod(sttMethod)
                .build();
    }

    private String performWhisperSTT(byte[] audioBytes, String lang) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + hfToken);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            String url = whisperApiUrl + "?language=" + lang;
            HttpEntity<byte[]> entity = new HttpEntity<>(audioBytes, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode node = objectMapper.readTree(response.getBody());
            return node.path("text").asText();

        } catch (Exception e) {
            log.error("Whisper STT 실패: {}", e.getMessage());
            throw new STTFailException("음성 인식 서비스 오류: " + e.getMessage());
        }
    }

    /**
     * 노이즈 레벨 분석 (RMS 에너지 기반, 0.0 ~ 1.0)
     */
    private double analyzeNoiseLevel(byte[] audioBytes) {
        if (audioBytes.length == 0) return 0.0;
        long sumSquares = 0;
        for (byte b : audioBytes) {
            sumSquares += (long) b * b;
        }
        double rms = Math.sqrt((double) sumSquares / audioBytes.length);
        return Math.min(rms / 128.0, 1.0);
    }

    public byte[] textToSpeech(String text, String lang) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + hfToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of("inputs", text);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            String modelUrl = getTtsModelUrl(lang);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    modelUrl, HttpMethod.POST, entity, byte[].class);

            return response.getBody();
        } catch (Exception e) {
            log.error("TTS 변환 실패: {}", e.getMessage());
            return new byte[0];
        }
    }

    private String getTtsModelUrl(String lang) {
        return switch (lang.toLowerCase()) {
            case "ko" -> "https://api-inference.huggingface.co/models/kakaoenterprise/vits-ljs";
            case "en" -> "https://api-inference.huggingface.co/models/facebook/mms-tts-eng";
            case "ja" -> "https://api-inference.huggingface.co/models/facebook/mms-tts-jpn";
            case "zh" -> "https://api-inference.huggingface.co/models/facebook/mms-tts-cmn";
            default   -> "https://api-inference.huggingface.co/models/facebook/mms-tts-eng";
        };
    }

    // ============================================================
    // SOS-01: 다국어 디지털 구조 요청
    // ============================================================

    public SosResponse processSosRequest(TranslationController.SosRequestDto dto) {
        String baseMessage = generateSosMessage(dto.getMessage(), dto.getSituationType());

        String translated = specialTermTranslate(
                baseMessage, "ko", dto.getTargetLanguage(),
                dto.getSituationType().toLowerCase()
        );

        SosRequest entity = SosRequest.builder()
                .userId(dto.getUserId())
                .situationType(dto.getSituationType())
                .originalMessage(baseMessage)
                .translatedMessage(translated)
                .targetLanguage(dto.getTargetLanguage())
                .outputType(SosRequest.OutputType.valueOf(dto.getOutputType()))
                .build();
        sosRequestRepository.save(entity);

        byte[] audioData = null;
        if (!"TEXT".equals(dto.getOutputType())) {
            audioData = textToSpeech(translated, dto.getTargetLanguage());
        }

        return SosResponse.builder()
                .requestId(entity.getId())
                .originalMessage(baseMessage)
                .translatedMessage(translated)
                .targetLanguage(dto.getTargetLanguage())
                .outputType(dto.getOutputType())
                .audioData(audioData)
                .displayText(buildDisplayText(translated, dto))
                .situationType(dto.getSituationType())
                .timestamp(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .success(true)
                .build();
    }

    private String generateSosMessage(String userMessage, String situationType) {
        String prefix = switch (situationType.toUpperCase()) {
            case "재난", "DISASTER" -> "🆘 긴급 재난 구조 요청: ";
            case "의료", "MEDICAL"  -> "🏥 긴급 의료 지원 요청: ";
            case "화재", "FIRE"     -> "🔥 화재 긴급 신고: ";
            case "홍수", "FLOOD"    -> "💧 홍수 구조 요청: ";
            default                 -> "🆘 긴급 구조 요청: ";
        };
        return prefix + userMessage;
    }

    private String buildDisplayText(String translated, TranslationController.SosRequestDto dto) {
        return String.format(
                "=== 구조 요청 ===\n%s\n\n위치: %.4f, %.4f\n상황: %s",
                translated, dto.getLatitude(), dto.getLongitude(), dto.getSituationType()
        );
    }

    public List<SosRequest> getSosHistory(String userId) {
        return sosRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ============================================================
    // Gemma 모델 호출
    // ============================================================

    private String callGemmaModel(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + hfToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "inputs", prompt,
                    "parameters", Map.of(
                            "max_new_tokens", 512,
                            "temperature", 0.3,
                            "do_sample", false,
                            "return_full_text", false
                    )
            );

            String url = "https://api-inference.huggingface.co/models/" + gemmaModelId;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                return root.get(0).path("generated_text").asText().trim();
            }
            return root.path("generated_text").asText().trim();

        } catch (Exception e) {
            log.error("Gemma 모델 호출 실패: {}", e.getMessage());
            throw new RuntimeException("번역 모델 오류: " + e.getMessage());
        }
    }

    private boolean isSupportedLanguage(String lang) {
        Set<String> supported = Set.of(
                "ko", "en", "ja", "zh", "fr", "de", "es", "ar", "ru", "pt", "vi", "th"
        );
        return supported.contains(lang.toLowerCase());
    }
}