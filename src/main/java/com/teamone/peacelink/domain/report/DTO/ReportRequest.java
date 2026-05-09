package com.teamone.peacelink.domain.report.DTO;

import com.teamone.peacelink.domain.report.Entity.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ReportRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    @NotNull(message = "제보 유형은 필수입니다.")
    private ReportType reportType;

    // 위치 (null 이면 user.lastLat/Lng 사용)
    private Double lat;
    private Double lng;

    // 텍스트 설명 (선택, 최대 200자)
    private String description;

    // ── 미디어 첨부 (모두 선택, Base64 인코딩) ─────────────────────────────

    // 이미지 (여러 장 가능)
    // MIME: image/jpeg, image/png, image/webp, image/gif
    private List<String> imageBase64List = new ArrayList<>();
    private String imageMimeType; // 미입력 시 기본값 image/jpeg

    // 동영상 (한 개)
    // MIME: video/mp4, video/mpeg, video/mov, video/webm, video/quicktime
    private String videoBase64;
    private String videoMimeType; // 미입력 시 기본값 video/mp4

    // 음성 (한 개)
    // MIME: audio/aac, audio/mp3, audio/wav, audio/flac, audio/ogg
    private String audioBase64;
    private String audioMimeType; // 미입력 시 기본값 audio/aac
}