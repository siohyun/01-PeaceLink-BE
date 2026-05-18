package com.teamone.peacelink.domain.shelter.Service;

import com.teamone.peacelink.domain.shelter.DTO.ShelterResponse;
import com.teamone.peacelink.domain.shelter.Entity.Shelter;
import com.teamone.peacelink.domain.shelter.Repository.ShelterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShelterService {

    private final ShelterRepository shelterRepository;

    public ShelterResponse getNearestShelter(Double lat, Double lng) {
        List<Shelter> shelters = shelterRepository.findNearestShelters(lat, lng, 1);
        if (shelters.isEmpty()) {
            throw new IllegalStateException("주변에 운영 가능한 대피소가 없습니다.");
        }
        Shelter nearest = shelters.get(0);
        return ShelterResponse.of(nearest, calculateDistance(lat, lng,
                nearest.getLat(), nearest.getLng()));
    }

    private Double calculateDistance(Double lat1, Double lng1,
                                     Double lat2, Double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public List<ShelterResponse> getNearbyShelters(Double lat, Double lng, int limit) {
        return shelterRepository.findNearestShelters(lat, lng, limit)
                .stream()
                .map(s -> ShelterResponse.of(s, calculateDistance(lat, lng, s.getLat(), s.getLng())))
                .toList();
    }
}