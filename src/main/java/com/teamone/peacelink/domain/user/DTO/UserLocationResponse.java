package com.teamone.peacelink.domain.user.DTO;

import com.teamone.peacelink.domain.user.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor  // new UserLocationResponse(lat, lng, null) 사용 가능하도록
public class UserLocationResponse {
    private Double lat;
    private Double lng;
    private String address; // 역지오코딩 결과, null 가능

    public static UserLocationResponse of(User user) {
        return UserLocationResponse.builder()
                .lat(user.getLastLat())
                .lng(user.getLastLng())
                .build();
    }
}