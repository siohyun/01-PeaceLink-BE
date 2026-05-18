package com.teamone.peacelink.domain.report.Entity;

public enum ReportStatus {
    PENDING,     // 분석 중
    VERIFIED,    // 검증 완료
    REJECTED,    // 출처 불분명 차단
    RESOLVED     // 처리 완료
}