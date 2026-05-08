package com.teamone.peacelink.domain.shelter.Service;

import com.teamone.peacelink.domain.shelter.DTO.ShelterItem;
import com.teamone.peacelink.domain.shelter.Entity.Shelter;
import com.teamone.peacelink.domain.shelter.ShelterApiClient;
import com.teamone.peacelink.domain.shelter.Repository.ShelterRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShelterSyncService {

    private final ShelterApiClient shelterApiClient;
    private final ShelterRepository shelterRepository;

    @Transactional
    public void sync() {
        List<ShelterItem> items = shelterApiClient.fetchAll();
        int inserted = 0, updated = 0, skipped = 0;

        for (ShelterItem item : items) {
            // 위도/경도 없는 항목 스킵
            if (item.getLat() == null || item.getLng() == null
                    || item.getLat().isBlank() || item.getLng().isBlank()) {
                skipped++;
                continue;
            }

            try {
                Double lat = Double.parseDouble(item.getLat());
                Double lng = Double.parseDouble(item.getLng());
                int capacity = item.getCapacity() != null && !item.getCapacity().isBlank()
                        ? Integer.parseInt(item.getCapacity().replaceAll("[^0-9]", ""))
                        : 0;

                // 운영상태 확인 (운영중만 isAvailable=true)
                boolean isAvailable = item.getOperStatus() == null
                        || item.getOperStatus().contains("운영");

                Optional<Shelter> existing = shelterRepository.findByCode(item.getCode());
                if (existing.isPresent()) {
                    existing.get().update(item.getName(), lat, lng, capacity);
                    updated++;
                } else {
                    shelterRepository.save(Shelter.builder()
                            .name(item.getName())
                            .code(item.getCode())
                            .lat(lat)
                            .lng(lng)
                            .capacity(capacity)
                            .isAvailable(isAvailable)
                            .build());
                    inserted++;
                }
            } catch (NumberFormatException e) {
                log.warn("대피소 데이터 파싱 실패 - code: {}", item.getCode());
                skipped++;
            }
        }
        log.info("대피소 동기화 완료 - 신규: {}, 수정: {}, 스킵: {}", inserted, updated, skipped);
    }
}
