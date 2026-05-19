package com.teamone.peacelink.domain.report.Client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamone.peacelink.domain.report.Entity.ReportType;
import com.teamone.peacelink.domain.report.Entity.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gemini API 통합 클라이언트
 *
 * ┌─ analyzeRisk() ─────────────────────────────────────────────────────────┐
 * │  이미지(JPEG/PNG) + 동영상(MP4) + 음성(AAC/MP3/WAV) + 텍스트           │
 * │  → Gemini 멀티모달 분석 → 위험 키워드 추출 → 위험도(LOW~CRITICAL) 산출  │
 * └──────────────────────────────────────────────────────────────────────────┘
 * ┌─ verifyWithGrounding() ──────────────────────────────────────────────────┐
 * │  제보 내용 → Gemini (google_search_retrieval) → 공식 출처 검색           │
 * │  → verified=true(요약 반환) / verified=false(차단)                       │
 * └──────────────────────────────────────────────────────────────────────────┘
 *
 * 모델: gemini-2.0-flash  (무료 티어 1,500회/일, 멀티모달 + Grounding 지원)
 * Gemini 지원 미디어 형식:
 *   이미지: image/jpeg, image/png, image/webp, image/gif
 *   동영상: video/mp4, video/mpeg, video/mov, video/webm  (최대 ~1GB, 1시간)
 *   음성:   audio/aac, audio/mp3, audio/wav, audio/flac, audio/ogg
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    // 위험도 분석: 멀티모달 지원 모델
    private static final String ANALYZE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // Grounding 검증: 동일 모델 (google_search 툴 지원)
    private static final String GROUNDING_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    // ── Result Records ───────────────────────────────────────────────────────

    public record RiskAnalysisResult(RiskLevel riskLevel, String reason) {}

    public record GroundingResult(boolean verified, String summary, String sources) {}

    // ════════════════════════════════════════════════════════════════════════
    // 1. 위험도 분석  (이미지 + 동영상 + 음성 + 텍스트 → LOW~CRITICAL)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * @param reportType        제보 유형 (화재/연기, 폭발/포격 등)
     * @param description       사용자 텍스트 설명 (nullable)
     * @param imageBase64List   이미지 Base64 목록 (mimeType: image/jpeg 등)
     * @param videoBase64       동영상 Base64 (nullable, mimeType: video/mp4 등)
     * @param audioBase64       음성 Base64 (nullable, mimeType: audio/aac 등)
     * @param imageMimeType     이미지 MIME (기본 image/jpeg)
     * @param videoMimeType     동영상 MIME (기본 video/mp4)
     * @param audioMimeType     음성 MIME (기본 audio/aac)
     */
    public RiskAnalysisResult analyzeRisk(ReportType reportType,
                                          String description,
                                          List<String> imageBase64List,
                                          String videoBase64,
                                          String audioBase64,
                                          String imageMimeType,
                                          String videoMimeType,
                                          String audioMimeType) {
        try {
            List<Map<String, Object>> parts = new ArrayList<>();

            // ① 이미지 파트 (여러 장 가능)
            String imgMime = imageMimeType != null ? imageMimeType : "image/jpeg";
            for (String b64 : imageBase64List) {
                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mime_type", imgMime,
                                "data", b64
                        )
                ));
            }

            // ② 동영상 파트 (한 개)
            if (videoBase64 != null && !videoBase64.isBlank()) {
                String vidMime = videoMimeType != null ? videoMimeType : "video/mp4";
                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mime_type", vidMime,
                                "data", videoBase64
                        )
                ));
            }

            // ③ 음성 파트 (한 개)
            if (audioBase64 != null && !audioBase64.isBlank()) {
                String audMime = audioMimeType != null ? audioMimeType : "audio/aac";
                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mime_type", audMime,
                                "data", audioBase64
                        )
                ));
            }

            // ④ 텍스트 프롬프트 (마지막)
            parts.add(Map.of("text", buildRiskPrompt(reportType, description)));

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", parts)),
                    "generationConfig", Map.of(
                            "temperature", 0.1,
                            "maxOutputTokens", 2048   // 응답 잘림 방지
                    )
            );

            String responseBody = restClient.post()
                    .uri(ANALYZE_URL + "?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseRiskResponse(responseBody);

        } catch (Exception e) {
            log.error("Gemini 위험도 분석 실패: {}", e.getMessage());
            return new RiskAnalysisResult(RiskLevel.HIGH, "자동 분석 실패 - 수동 검토 필요");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // 2. Grounding 검증  (텍스트 + Google Search → verified true/false)
    // ════════════════════════════════════════════════════════════════════════

    public GroundingResult verifyWithGrounding(ReportType reportType,
                                               String description,
                                               Double lat, Double lng) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", buildGroundingPrompt(reportType, description, lat, lng))
                            ))
                    ),
                    "tools", List.of(
                            Map.of("google_search", Map.of())  // gemini-2.5-flash 신규 툴명
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.1,
                            "maxOutputTokens", 2048
                    )
            );

            String responseBody = restClient.post()
                    .uri(GROUNDING_URL + "?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return parseGroundingResponse(responseBody);

        } catch (Exception e) {
            log.error("Gemini Grounding 검증 실패: {}", e.getMessage());
            return new GroundingResult(false, "검증 서비스 일시 불가 - 수동 검토 예정", "[]");
        }
    }

    // ── 프롬프트 빌더 ────────────────────────────────────────────────────────

    private String buildRiskPrompt(ReportType reportType, String description) {
        return """
                당신은 재난 위험도 분석 전문가입니다.
                첨부된 미디어(이미지/동영상/음성)와 아래 제보 정보를 종합하여 위험도를 판정하세요.

                [제보 유형] %s
                [텍스트 설명] %s

                분석 지침:
                - 이미지/동영상: 연기 색상·밀도, 화염 규모, 건물 손상, 부상자 여부 등 시각적 단서 반영
                - 음성: 폭발음, 총성, 비명, 사이렌 등 청각적 단서 반영
                - 텍스트: 위험 키워드(불길, 연기, 폭발, 붕괴, 부상자 등) 반영

                위험도 기준:
                - LOW(낮음): 경미한 위험, 즉각 대응 불필요
                - MEDIUM(보통): 주의 필요, 모니터링 권고
                - HIGH(높음): 위험 상황, 대피 고려 필요
                - CRITICAL(위급): 즉각 대피, 응급 대응 필요

                반드시 아래 JSON 형식으로만 응답하세요 (마크다운 코드블록 없이 순수 JSON):
                {
                  "riskLevel": "LOW|MEDIUM|HIGH|CRITICAL 중 하나",
                  "keywords": ["감지된 위험 키워드"],
                  "reason": "판정 근거를 한국어로 작성"
                }
                """.formatted(
                reportType.getDisplayName(),
                description != null ? description : "없음"
        );
    }

    private String buildGroundingPrompt(ReportType reportType, String description,
                                        Double lat, Double lng) {
        String today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        return """
                당신은 재난 정보 검증 전문가입니다.
                아래 제보 내용이 신뢰할 수 있는 공식 출처에서 확인 가능한지 Google 검색으로 검증해주세요.

                [오늘 날짜] %s
                [제보 유형] %s
                [위치] 위도 %.6f, 경도 %.6f (대한민국)
                [내용] %s

                검증 기준:
                1. 반드시 오늘(%s) 보도된 기사 및 알림만 인정합니다. 과거 날짜 기사는 무조건 무시하세요.
                2. 공식 뉴스 매체(연합뉴스, YTN, KBS, MBC, SBS 등)에서 오늘 날짜로 동일/유사 사건 보도 여부
                3. 정부/지자체 공식 재난 알림(국민재난안전포털, 소방청 등) 오늘 날짜 발령 여부
                4. 오늘 날짜 공식 출처 확인 불가 → 반드시 verified: false로 응답

                반드시 아래 JSON 형식으로만 응답하세요 (마크다운 코드블록 없이 순수 JSON):
                {
                  "verified": true,
                  "summary": "공식 출처에서 확인된 내용 요약 (2~3문장, 출처 날짜 포함)",
                  "sources": ["https://출처1", "https://출처2"],
                  "blockReason": null
                }

                오늘 날짜 공식 출처 확인 불가 시:
                {
                  "verified": false,
                  "summary": null,
                  "sources": [],
                  "blockReason": "오늘 날짜의 공식 출처에서 확인할 수 없는 정보입니다."
                }
                """.formatted(
                today,
                reportType.getDisplayName(), lat, lng,
                description != null ? description : "없음",
                today
        );
    }

    // ── 응답 파서 ────────────────────────────────────────────────────────────

    private RiskAnalysisResult parseRiskResponse(String responseBody) throws Exception {
        String text = extractGeminiText(responseBody);
        JsonNode result = objectMapper.readTree(extractJson(text));
        RiskLevel level = RiskLevel.valueOf(result.get("riskLevel").asText());
        String reason = result.get("reason").asText();
        return new RiskAnalysisResult(level, reason);
    }

    private GroundingResult parseGroundingResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        // google_search 툴 사용 시 응답 구조:
        // candidates[0].content.parts[] 안에 text 타입 블록이 여러 개일 수 있음
        // → text 타입 블록을 모두 합쳐서 JSON 추출
        String text = extractAllText(root);
        log.debug("Grounding 응답 전체 텍스트: {}", text);

        // groundingMetadata: google_search 사용 시 실제 검색 출처
        JsonNode candidate = root.path("candidates").get(0);
        JsonNode groundingMeta = candidate != null
                ? candidate.path("groundingMetadata")
                : objectMapper.createObjectNode();

        try {
            // 마크다운 코드블록 제거 (```json ... ``` 형태로 올 때)
            String cleaned = text.replaceAll("(?s)```[a-z]*\\s*", "").replace("```", "").trim();
            JsonNode result = objectMapper.readTree(extractJson(cleaned));
            boolean verified = result.path("verified").asBoolean(false);
            String summary = verified
                    ? result.path("summary").asText("요약 없음")
                    : result.path("blockReason").asText("공식 출처 미확인");

            String sourcesFromJson = result.has("sources") ? result.get("sources").toString() : "[]";
            String sourcesFromMeta = extractGroundingSources(groundingMeta);
            String finalSources = "[]".equals(sourcesFromMeta) ? sourcesFromJson : sourcesFromMeta;

            return new GroundingResult(verified, summary, finalSources);

        } catch (Exception e) {
            // JSON 파싱 실패 시 → Gemini 응답 텍스트 자체를 요약으로 사용하고 미검증 처리
            log.warn("Grounding JSON 파싱 실패, 텍스트 응답으로 대체: {}", e.getMessage());
            String sources = extractGroundingSources(groundingMeta);
            boolean hasSource = !"[]".equals(sources);
            return new GroundingResult(hasSource,
                    text.length() > 300 ? text.substring(0, 300) + "..." : text,
                    sources);
        }
    }

    // Gemini 응답에서 text 타입 파트를 모두 합쳐서 반환
    // (google_search 툴 사용 시 parts가 [tool_use, text] 순서로 올 수 있음)
    private String extractAllText(JsonNode root) {
        try {
            JsonNode parts = root.path("candidates").get(0)
                    .path("content").path("parts");
            StringBuilder sb = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    sb.append(part.get("text").asText());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // analyzeRisk 에서 사용하는 단순 텍스트 추출 (parts[0].text)
    private String extractGeminiText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return extractAllText(root);
    }

    private String extractGroundingSources(JsonNode groundingMeta) {
        if (groundingMeta.isMissingNode()) return "[]";
        try {
            List<String> urls = new ArrayList<>();
            for (JsonNode chunk : groundingMeta.path("groundingChunks")) {
                String uri = chunk.path("web").path("uri").asText("");
                if (!uri.isBlank()) urls.add("\"" + uri + "\"");
            }
            return "[" + String.join(",", urls) + "]";
        } catch (Exception e) {
            return "[]";
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        throw new IllegalStateException("Gemini JSON 파싱 실패: " + text);
    }
}