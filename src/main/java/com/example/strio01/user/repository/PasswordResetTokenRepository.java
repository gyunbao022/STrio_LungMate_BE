package com.example.strio01.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.strio01.user.entity.PasswordResetTokenEntity;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, String> {
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);
}
