package com.teamone.peacelink.domain.user.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Request
@Getter
@NoArgsConstructor
public class DeviceRegisterRequest {
    @NotBlank
    private String deviceId;
    private String language;
}