package com.teamone.peacelink.domain.user.Service;

import com.teamone.peacelink.domain.user.DTO.DeviceRegisterRequest;
import com.teamone.peacelink.domain.user.DTO.DeviceRegisterResponse;
import com.teamone.peacelink.domain.user.Entity.User;
import com.teamone.peacelink.domain.user.Repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

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

    public void updateLocation(UUID userId, Double lat, Double lng) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.updateLocation(lat, lng);
    }
}
