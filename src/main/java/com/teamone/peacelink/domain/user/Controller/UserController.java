package com.teamone.peacelink.domain.user.Controller;

import com.teamone.peacelink.domain.user.DTO.DeviceRegisterRequest;
import com.teamone.peacelink.domain.user.DTO.DeviceRegisterResponse;
import com.teamone.peacelink.domain.user.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register-device")
    public ResponseEntity<DeviceRegisterResponse> registerDevice(
            @RequestBody @Valid DeviceRegisterRequest request) {
        return ResponseEntity.ok(userService.upsertByDeviceId(request));
    }

    @PatchMapping("/{userId}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable UUID userId,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        userService.updateLocation(userId, lat, lng);
        return ResponseEntity.ok().build();
    }
}