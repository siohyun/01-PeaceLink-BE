package com.teamone.peacelink.domain.report.Controller;

import com.teamone.peacelink.domain.report.DTO.ReportResponse;
import com.teamone.peacelink.domain.report.Entity.ReportType;
import com.teamone.peacelink.domain.report.Service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * POST /api/reports
     * Content-Type: multipart/form-data
     *
     * 필드:
     *   userId      (필수) UUID
     *   reportType  (필수) FIRE_SMOKE | EXPLOSION_ATTACK | RESCUE_REQUEST | ROAD_CONTROL | OTHER_DANGER
     *   lat         (선택) Double
     *   lng         (선택) Double
     *   description (선택) String 최대 200자
     *   images      (선택) 이미지 파일 최대 4장
     *   video       (선택) 동영상 파일 1개
     *   audio       (선택) 음성 파일 1개
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> createReport(
            @RequestParam("userId")                                UUID userId,
            @RequestParam("reportType")                            ReportType reportType,
            @RequestParam(value = "lat",         required = false) Double lat,
            @RequestParam(value = "lng",         required = false) Double lng,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "images",       required = false) List<MultipartFile> images,
            @RequestPart(value = "video",        required = false) MultipartFile video,
            @RequestPart(value = "audio",        required = false) MultipartFile audio
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reportService.createReport(
                        userId, reportType, lat, lng, description, images, video, audio));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReportResponse>> getReportsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(reportService.getReportsByUser(userId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ReportResponse>> getNearbyReports(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        return ResponseEntity.ok(reportService.getNearbyVerifiedReports(lat, lng, radiusKm));
    }
}