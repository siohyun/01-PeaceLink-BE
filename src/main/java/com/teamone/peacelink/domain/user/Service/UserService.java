package com.teamone.peacelink.domain.user.Service;

import com.teamone.peacelink.domain.user.DTO.DeviceRegisterRequest;
import com.teamone.peacelink.domain.user.DTO.DeviceRegisterResponse;
import com.teamone.peacelink.domain.user.DTO.UserLocationResponse;
import com.teamone.peacelink.domain.user.Entity.User;
import com.teamone.peacelink.domain.user.Repository.UserRepository;
import com.teamone.peacelink.global.Map.KakaoMapClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.teamone.peacelink.global.Map.KakaoAddressResponse;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KakaoMapClient kakaoMapClient;

    // ── 디바이스 등록 / 재등록 ────────────────────────────────────────────────

    public DeviceRegisterResponse upsertByDeviceId(DeviceRegisterRequest request) {
        Optional<User> existing = userRepository.findByDeviceId(request.getDeviceId());
        if (existing.isPresent()) {
            existing.get().updateLastSeen();
            return DeviceRegisterResponse.of(existing.get(), false);
        }
        User newUser = User.builder()
                .deviceId(request.getDeviceId())
                .language(request.getLanguage())
                .build();
        return DeviceRegisterResponse.of(userRepository.save(newUser), true);
    }

    // ── 위치 조회 ─────────────────────────────────────────────────────────────

    public UserLocationResponse getLocation(UUID userId) {
        User user = findById(userId);
        if (user.getLastLat() == null || user.getLastLng() == null) {
            throw new IllegalStateException("저장된 위치 정보가 없습니다.");
        }
        return UserLocationResponse.of(user);
    }

    // ── 위치 업데이트 + 역지오코딩 ───────────────────────────────────────────

    public UserLocationResponse updateLocationWithAddress(UUID userId, Double lat, Double lng) {
        User user = findById(userId);
        user.updateLocation(lat, lng);

        String address = null;

        try {

            KakaoAddressResponse.Address kakaoAddress =
                    kakaoMapClient.reverseGeocode(lat, lng);

            if (kakaoAddress != null) {

                address =
                        kakaoAddress.getRegion_1depth_name() + " "
                                + kakaoAddress.getRegion_2depth_name() + " "
                                + kakaoAddress.getRegion_3depth_name();
            }

        } catch (Exception e) {

            log.warn("역지오코딩 실패: {}", e.getMessage());
        }

        return UserLocationResponse.builder()
                .lat(lat)
                .lng(lng)
                .address(address)
                .build();
    }

    // ── 내부 공통 헬퍼 ───────────────────────────────────────────────────────

    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}