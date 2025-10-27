package com.example.strio01.user.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.strio01.user.entity.AdminRefreshTokenEntity;
import com.example.strio01.user.repository.AdminRefreshTokenRepository;


import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
	
   private final AdminRefreshTokenRepository tokenRepository;
   
   public void saveRefreshToken(String adminId, String refreshToken, String ip, String userAgent) {
       tokenRepository.findByAdminId(adminId).ifPresentOrElse(
           existing -> {
               existing.setRefreshToken(refreshToken);
               existing.setIpAddress(ip);
               existing.setUserAgent(userAgent);
               existing.setCreatedAt(LocalDateTime.now());
               tokenRepository.save(existing);
           },
           () -> {
               tokenRepository.save(AdminRefreshTokenEntity.builder()
                       .adminId(adminId)
                       .refreshToken(refreshToken)
                       .ipAddress(ip)
                       .userAgent(userAgent)
                       .createdAt(LocalDateTime.now())
                       .build());
           }
       );
   }
   public boolean validateRefreshToken(String adminId, String token) {
       return tokenRepository.findByAdminId(adminId)
               .map(stored -> stored.getRefreshToken().equals(token))
               .orElse(false);
   }
   public void deleteRefreshToken(String adminId) {    	   
       tokenRepository.deleteByAdminId(adminId);
   }
   
}




