package com.example.strio01.user.service;

public interface AuthService {
    void saveRefreshToken(String userId, String refreshToken , String ip, String userAgent);
    boolean validateRefreshToken(String userId, String refreshToken);
    void deleteRefreshToken(String userId);
}
