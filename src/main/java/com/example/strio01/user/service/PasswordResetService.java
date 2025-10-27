// PasswordResetService.java
package com.example.strio01.user.service;

import com.example.strio01.user.entity.PasswordResetTokenEntity;
import com.example.strio01.user.entity.UserInfoEntity;
import com.example.strio01.user.repository.PasswordResetTokenRepository;
import com.example.strio01.user.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserInfoRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 토큰 생성 & 저장: 원문 반환(이건 메일 링크용) */
    @Transactional
    public String createResetToken(String userId) {
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256(rawToken);

        PasswordResetTokenEntity token = PasswordResetTokenEntity.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .used(false)
                .createdAt(Instant.now())
                .build();
        tokenRepo.save(token);
        return rawToken; // 메일로 보낼 것은 "원문"
    }

    /** 토큰 검증 & 비밀번호 변경(1회용) */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = sha256(rawToken);
        PasswordResetTokenEntity token = tokenRepo.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

        if (token.isUsed()) throw new RuntimeException("이미 사용된 토큰입니다.");
        if (token.getExpiresAt().isBefore(Instant.now())) throw new RuntimeException("만료된 토큰입니다.");

        UserInfoEntity user = userRepo.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        user.setPasswd(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        token.setUsed(true);
        tokenRepo.save(token);
    }
}
