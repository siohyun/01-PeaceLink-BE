package com.teamone.peacelink.app.triage;

import com.teamone.peacelink.app.user.User;
import com.teamone.peacelink.app.user.UserRepository;
import com.teamone.peacelink.app.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/triage")
@RequiredArgsConstructor
public class TriageController {

    private final TriageRecordRepository triageRecordRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    @PostMapping("/analyze")
    public ResponseEntity<TriageRecord> analyze(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("userId"));
        String imageUrl = request.get("imageUrl");
        String base64Image = request.get("base64Image");

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not Found"));

        String prompt = "이 환자의 상처를 판독해 부상 등급을 RED(긴급), YELLOW(응급), GREEN(비응급) 중 하나로 판정하고 " +
                "응급처치법을 한 문장으로 알려주세요. 응답 형식: '등급: [등급] | 처치법: [처치법내용]'";

        String aiResult = geminiService.callGeminiWithImage(prompt, "image/jpeg", base64Image);

        String triageLevel = "GREEN";
        String firstAidTip = aiResult;

        if (aiResult.contains("등급:")) {
            try {
                triageLevel = aiResult.split("\\|")[0].replace("등급:", "").trim();
                firstAidTip = aiResult.split("\\|")[1].replace("처치법:", "").trim();
            } catch (Exception ignored) {}
        }

        TriageRecord record = TriageRecord.builder()
                .user(user).imageUrl(imageUrl).triageLevel(triageLevel)
                .symptoms("이미지 판독 결과").firstAidTip(firstAidTip)
                .build();

        return ResponseEntity.ok(triageRecordRepository.save(record));
    }
}