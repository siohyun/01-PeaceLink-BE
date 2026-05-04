package com.teamone.peacelink.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callGemini(String prompt) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GEMINI_URL + apiKey;

            Map<String, Object> body = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", Collections.singletonList(part));
            body.put("contents", Collections.singletonList(contents));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "AI Error: " + e.getMessage();
        }
    }

    public String callGeminiWithImage(String prompt, String mimeType, String base64Image) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GEMINI_URL + apiKey;

            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();

            Map<String, Object> textPart = new HashMap<>();
            textPart.put("text", prompt);
            parts.add(textPart);

            Map<String, Object> imagePart = new HashMap<>();
            Map<String, String> inlineData = new HashMap<>();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);
            imagePart.put("inline_data", inlineData);
            parts.add(imagePart);

            Map<String, Object> contents = new HashMap<>();
            contents.put("parts", parts);
            body.put("contents", Collections.singletonList(contents));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "AI Image Error: " + e.getMessage();
        }
    }
}