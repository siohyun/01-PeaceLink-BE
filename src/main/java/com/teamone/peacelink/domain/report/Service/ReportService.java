package com.teamone.peacelink.domain.report.Service;

import com.teamone.peacelink.domain.report.Client.GeminiClient;
import com.teamone.peacelink.domain.report.Client.GeminiClient.GroundingResult;
import com.teamone.peacelink.domain.report.Client.GeminiClient.RiskAnalysisResult;
import com.teamone.peacelink.domain.report.DTO.ReportResponse;
import com.teamone.peacelink.domain.report.Entity.Report;
import com.teamone.peacelink.domain.report.Entity.ReportStatus;
import com.teamone.peacelink.domain.report.Entity.ReportType;
import com.teamone.peacelink.domain.report.Repository.ReportRepository;
import com.teamone.peacelink.domain.user.Entity.User;
import com.teamone.peacelink.domain.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final GeminiClient geminiClient;
    private final S3MediaService s3MediaService;

    /**
     * 제보 생성 전체 흐름:
     * 1. 사용자 조회 + 위치 확정
     * 2. 미디어 파일 → S3 업로드(URL) + Base64 변환(Gemini용) 동시 처리
     * 3. Gemini 멀티모달 → 위험도 분석
     * 4. Gemini Grounding → 오늘 날짜 공식 출처 검증
     * 5. DB 저장
     */
    public ReportResponse createReport(UUID userId,
                                       ReportType reportType,
                                       Double lat, Double lng,
                                       String description,
                                       List<MultipartFile> images,
                                       MultipartFile video,
                                       MultipartFile audio) {

        // 1. 사용자 조회 및 위치 확정
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Double finalLat = lat != null ? lat : user.getLastLat();
        Double finalLng = lng != null ? lng : user.getLastLng();
        if (finalLat == null || finalLng == null) {
            throw new IllegalStateException("현재 위치를 확인할 수 없습니다. 위치를 입력해주세요.");
        }

        // 2. 미디어 S3 업로드 + Base64 변환
        List<String> mediaUrls   = new ArrayList<>();
        List<String> imageBase64 = new ArrayList<>();
        String imageMime = "image/jpeg";
        String videoBase64 = null, videoMime = null;
        String audioBase64 = null, audioMime = null;

        // 이미지 (최대 4장)
        if (images != null) {
            for (MultipartFile img : images) {
                if (img.isEmpty()) continue;
                var result = s3MediaService.upload(img, "reports/images");
                mediaUrls.add(result.s3Url());
                imageBase64.add(result.base64());
                imageMime = result.mimeType();
            }
        }

        // 동영상
        if (video != null && !video.isEmpty()) {
            var result = s3MediaService.upload(video, "reports/videos");
            mediaUrls.add(result.s3Url());
            videoBase64 = result.base64();
            videoMime   = result.mimeType();
        }

        // 음성
        if (audio != null && !audio.isEmpty()) {
            var result = s3MediaService.upload(audio, "reports/audio");
            mediaUrls.add(result.s3Url());
            audioBase64 = result.base64();
            audioMime   = result.mimeType();
        }

        // 3. Gemini 위험도 분석
        log.info("[제보] 위험도 분석 시작 - type={}, 이미지={}장, 동영상={}, 음성={}",
                reportType, imageBase64.size(),
                videoBase64 != null, audioBase64 != null);

        RiskAnalysisResult riskResult = geminiClient.analyzeRisk(
                reportType, description,
                imageBase64, videoBase64, audioBase64,
                imageMime, videoMime, audioMime
        );
        log.info("[제보] 위험도 분석 완료 - level={}", riskResult.riskLevel());

        // 4. Gemini Grounding 검증
        log.info("[제보] Grounding 검증 시작");
        GroundingResult groundingResult = geminiClient.verifyWithGrounding(
                reportType, description, finalLat, finalLng);
        log.info("[제보] Grounding 검증 완료 - verified={}", groundingResult.verified());

        ReportStatus status = groundingResult.verified()
                ? ReportStatus.VERIFIED : ReportStatus.REJECTED;

        // 5. DB 저장
        Report report = Report.builder()
                .user(user)
                .reportType(reportType)
                .lat(finalLat)
                .lng(finalLng)
                .description(description)
                .riskLevel(riskResult.riskLevel())
                .riskReason(riskResult.reason())
                .verified(groundingResult.verified())
                .verifiedSummary(groundingResult.summary())
                .groundingSources(groundingResult.sources())
                .mediaUrls(mediaUrls)
                .status(status)
                .build();

        user.updateLocation(finalLat, finalLng);
        return ReportResponse.of(reportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByUser(UUID userId) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(ReportResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getNearbyVerifiedReports(Double lat, Double lng, Double radiusKm) {
        return reportRepository.findVerifiedReportsNearby(lat, lng, radiusKm)
                .stream().map(ReportResponse::of).toList();
    }
}