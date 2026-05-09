package com.teamone.peacelink.domain.shelter.Controller;

import com.teamone.peacelink.domain.shelter.Service.ShelterSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/shelter")
@RequiredArgsConstructor
public class ShelterAdminController {

    private final ShelterSyncService shelterSyncService;

    @PostMapping("/sync")
    public ResponseEntity<String> sync() {
        shelterSyncService.sync();
        return ResponseEntity.ok("대피소 동기화 완료");
    }
}