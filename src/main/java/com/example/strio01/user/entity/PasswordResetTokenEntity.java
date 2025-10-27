// PasswordResetTokenEntity.java
package com.example.strio01.user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter
@Entity @Table(name="password_reset_token")
@NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    /** 안전하게 하려면 토큰 원문 대신 해시(예: SHA-256) 저장 권장 */
    @Column(unique = true, length = 128)
    private String tokenHash;

    private Instant expiresAt;

    private boolean used;

    private Instant createdAt;
}
