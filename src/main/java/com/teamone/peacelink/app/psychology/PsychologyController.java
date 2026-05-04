package com.teamone.peacelink.app.psychology;

import com.teamone.peacelink.app.user.User;
import com.teamone.peacelink.app.user.UserRepository;
import com.teamone.peacelink.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/psychology")
@RequiredArgsConstructor
public class PsychologyController {

    private final PsychologicalLogRepository psychologicalLogRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    @PostMapping("/care")
    public ResponseEntity<PsychologicalLog> care(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String userMessage = request.get("userMessage").toString();

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        String prompt = String.format(
                "사용자 말: '%s'. " +
                        "이 말에서 느껴지는 불안 수치를 0~100 사이 숫자로 환산하고, 안정을 주는 호흡법이나 그라운딩 위로 가이드를 한 문장으로 제공하세요. " +
                        "응답 형식: '점수: [숫자] | 답변: [가이드내용]'", userMessage);

        String aiResponse = geminiService.callGemini(prompt);
        int score = 50;

        if (aiResponse.contains("점수:")) {
            try {
                score = Integer.parseInt(aiResponse.split("\\|")[0].replace("점수:", "").trim());
            } catch (Exception ignored) {}
        }

        PsychologicalLog log = PsychologicalLog.builder()
                .user(user).anxietyScore(score).userMessage(userMessage).aiResponse(aiResponse)
                .build();

        return ResponseEntity.ok(psychologicalLogRepository.save(log));
    }
}