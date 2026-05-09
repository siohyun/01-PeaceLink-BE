package com.teamone.peacelink.domain.user.Repository;

import com.teamone.peacelink.domain.user.Entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByDeviceId(String deviceId);
}