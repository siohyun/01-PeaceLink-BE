package com.teamone.peacelink.domain.shelter;

import com.teamone.peacelink.domain.shelter.Service.ShelterSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShelterSyncScheduler {

    private final ShelterSyncService shelterSyncService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시
    public void scheduledSync() {
        log.info("대피소 자동 동기화 시작");
        shelterSyncService.sync();
    }
}
