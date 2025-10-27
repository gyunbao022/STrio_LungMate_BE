package com.example.strio01.user.service;

import java.util.List;
import java.util.Optional;

import com.example.strio01.user.dto.AuthInfo;
import com.example.strio01.user.dto.UserInfoDTO;
import com.example.strio01.user.entity.UserInfoEntity;

public interface UserInfoService {

    // 사용자 목록
    List<UserInfoDTO> listAllUsers();

    // 단일 사용자 조회
    UserInfoDTO getUser(String userId);

    // 회원가입 (비밀번호 암호화)
    AuthInfo createUserProcess(UserInfoDTO dto);

    // 로그인 처리 (비밀번호 검증 및 AuthInfo 반환)
    AuthInfo loginProcess(String userId, String rawPassword);

    // 회원정보 수정
    AuthInfo updateUserProcess(UserInfoDTO dto);
    AuthInfo updateUserPasswd(UserInfoDTO dto);
    
    // UserInfoService.java
    AuthInfo updateUserRoleProcess(UserInfoDTO dto);

    // 회원 삭제
    void deleteUser(String userId);

    // ✅ 사용자 수 카운트
    long countUsers();

    // ✅ ID로 조회 (RefreshToken 검증 등에서 사용)
    Optional<UserInfoEntity> findById(String userId);
}
