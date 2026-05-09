package com.teamone.peacelink.domain.report.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

/**
 * S3 업로드 + Gemini 분석용 Base64 변환을 함께 처리
 *
 * 흐름:
 *   MultipartFile → S3 업로드(URL 저장용) + Base64 변환(Gemini 분석용)
 *   → S3 URL은 DB mediaUrls에 저장
 *   → Base64는 GeminiClient.analyzeRisk() 에 전달
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3MediaService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.region}")
    private String region;

    public record UploadResult(String s3Url, String base64, String mimeType) {}

    /**
     * MultipartFile → S3 업로드 후 URL + Base64 반환
     * @param file     업로드할 파일
     * @param folder   S3 경로 prefix (예: "images", "videos", "audio")
     */
    public UploadResult upload(MultipartFile file, String folder) {
        try {
            String key = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            String mimeType = file.getContentType() != null
                    ? file.getContentType() : "application/octet-stream";

            // S3 업로드
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(mimeType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucket, region, key);

            // Gemini 분석용 Base64 변환
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            log.info("S3 업로드 완료: {}", s3Url);
            return new UploadResult(s3Url, base64, mimeType);

        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 실패: " + e.getMessage(), e);
        }
    }

    /** S3 파일 삭제 (URL에서 key 추출) */
    public void delete(String s3Url) {
        try {
            String key = s3Url.substring(s3Url.indexOf(".amazonaws.com/") + 15);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket).key(key).build());
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패: {}", e.getMessage());
        }
    }
}