package com.teamone.peacelink.domain.shelter.Controller;

import com.teamone.peacelink.domain.shelter.DTO.ShelterResponse;
import com.teamone.peacelink.domain.shelter.Service.ShelterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evacuation")
@RequiredArgsConstructor
public class ShelterController {

    private final ShelterService shelterService;

    @GetMapping("/shelters/nearest")
    public ResponseEntity<ShelterResponse> getNearestShelter(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        return ResponseEntity.ok(shelterService.getNearestShelter(lat, lng));
    }
}

