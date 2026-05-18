package com.teamone.peacelink.domain.report.Entity;

public enum ReportType {
    FIRE_SMOKE("화재 / 연기"),
    EXPLOSION_ATTACK("폭발 / 포격"),
    RESCUE_REQUEST("구조 요청"),
    ROAD_CONTROL("도로 통제"),
    FLOOD("침수 / 홍수"),       // 추가
    OTHER_DANGER("기타 위험");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}