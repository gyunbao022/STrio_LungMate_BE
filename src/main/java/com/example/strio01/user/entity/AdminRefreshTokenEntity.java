package com.example.strio01.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "admin_refresh_tokens")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class AdminRefreshTokenEntity {
	@Id
	   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_seq")
	   @SequenceGenerator(name = "refresh_token_seq", sequenceName = "admin_refresh_tokens_seq", allocationSize = 1)
	   @Column(name = "refresh_num")
	   private Long id;
	   @Column(name = "adminId", nullable = false)
	   private String adminId;
	   @Lob
	   @Column(name = "refresh_token", nullable = false)
	   private String refreshToken;
	   @Column(name = "user_agent")
	   private String userAgent;
	   @Column(name = "ip_address")
	   private String ipAddress;
	   @Column(name = "created_at", updatable = false)
	   private LocalDateTime createdAt;
}
