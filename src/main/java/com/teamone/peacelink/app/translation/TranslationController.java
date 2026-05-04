package com.teamone.peacelink.app.translation;

import com.teamone.peacelink.app.user.User;
import com.teamone.peacelink.app.user.UserRepository;
import com.teamone.peacelink.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final SosRequestRepository sosRequestRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String targetLang = request.getOrDefault("targetLang", "Korean");
        String prompt = String.format("너는 재난 및 구호 전문 통역사입니다. 다음 텍스트를 '%s'로 번역하세요: %s", targetLang, text);
        return ResponseEntity.ok(geminiService.callGemini(prompt));
    }

    @PostMapping("/sos")
    public ResponseEntity<SosRequest> createSos(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        Double lat = Double.valueOf(request.get("latitude").toString());
        Double lon = Double.valueOf(request.get("longitude").toString());
        String status = request.get("statusCode").toString();
        String originalMsg = request.get("originalMsg").toString();

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        String prompt = "구조대를 위해 다음 긴급 요청을 영어와 한국어로 번역 및 요약하세요: " + originalMsg;
        String translatedMsg = geminiService.callGemini(prompt);

        SosRequest sosRequest = SosRequest.builder()
                .user(user).latitude(lat).longitude(lon)
                .statusCode(status).originalMsg(originalMsg).translatedMsg(translatedMsg)
                .build();

        return ResponseEntity.ok(sosRequestRepository.save(sosRequest));
    }
}