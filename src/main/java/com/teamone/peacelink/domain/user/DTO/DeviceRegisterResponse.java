package com.teamone.peacelink.domain.user.DTO;

import com.teamone.peacelink.domain.user.Entity.User;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

// Response
@Getter
@Builder
public class DeviceRegisterResponse  {
    private UUID userId;
    private String deviceId;
    private String name;
    private String language;
    private boolean isNewUser;

    public static DeviceRegisterResponse of(User user, boolean isNewUser) {
        return DeviceRegisterResponse.builder()
                .userId(user.getId())
                .deviceId(user.getDeviceId())
                .name(user.getName())
                .language(user.getLanguage())
                .isNewUser(isNewUser)
                .build();
    }
}