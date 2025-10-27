package com.example.strio01.user.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.strio01.user.dto.AuthInfo;
import com.example.strio01.user.dto.UserInfoDTO;
import com.example.strio01.user.entity.UserInfoEntity;
import com.example.strio01.user.repository.UserInfoRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserInfoServiceImp implements UserInfoService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ✅ 전체 사용자 목록
    @Transactional
    @Override
    public List<UserInfoDTO> listAllUsers() {
        return userInfoRepository.findAll()
                .stream()
                .map(UserInfoDTO::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ 단일 사용자 조회
    @Transactional
    @Override
    public UserInfoDTO getUser(String userId) {
        return userInfoRepository.findById(userId)
                .map(UserInfoDTO::toDTO)
                .orElse(null);
    }

    // ✅ 회원가입 처리
    @Transactional
    @Override
    public AuthInfo createUserProcess(UserInfoDTO dto) {
        dto.setPasswd(passwordEncoder.encode(dto.getPasswd()));
        dto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        dto.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        UserInfoEntity entity = dto.toEntity();
        userInfoRepository.save(entity);

        return AuthInfo.builder()
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .roleCd(entity.getRoleCd())
                .build();
    }

    // ✅ 로그인 처리
    @Transactional
    @Override
    public AuthInfo loginProcess(String userId, String rawPassword) {
        log.info("로그인 시도: {}", userId);
        UserInfoEntity user = userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(rawPassword, user.getPasswd())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return AuthInfo.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .roleCd(user.getRoleCd())
                .build();
    }

    // ✅ 회원정보 수정
    @Transactional
    @Override
    public AuthInfo updateUserProcess(UserInfoDTO dto) {
        UserInfoEntity existing = userInfoRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("수정할 사용자가 존재하지 않습니다."));

        existing.setUserName(dto.getUserName());
        existing.setEmail(dto.getEmail());
        //existing.setRoleCd(dto.getRoleCd());
        existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // 비밀번호 변경 시 암호화 처리
        //if (dto.getPasswd() != null && !dto.getPasswd().isEmpty()) {
        //    existing.setPasswd(passwordEncoder.encode(dto.getPasswd()));
        //}

        userInfoRepository.save(existing);

        return AuthInfo.builder()
                .userId(existing.getUserId())
                .userName(existing.getUserName())
                .roleCd(existing.getRoleCd())
                .email(existing.getEmail())
                .build();
    }
    
    // 회원비번 수정
    @Transactional
    @Override
    public AuthInfo updateUserPasswd(UserInfoDTO dto) {
        UserInfoEntity existing = userInfoRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("수정할 사용자가 존재하지 않습니다."));
        
        // 기존 비밀번호 검증
        if (dto.getPasswd() == null || dto.getPasswd().isEmpty()) {
            throw new RuntimeException("현재 비밀번호를 입력해주세요.");
        }

        if (!passwordEncoder.matches(dto.getPasswd(), existing.getPasswd())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        if (dto.getNewPasswd() == null || dto.getNewPasswd().isEmpty()) {
            throw new RuntimeException("새 비밀번호를 입력해주세요.");
        }
        
        existing.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        existing.setPasswd(passwordEncoder.encode(dto.getNewPasswd()));

        userInfoRepository.save(existing);

        return AuthInfo.builder()
                .userId(existing.getUserId())
                .userName(existing.getUserName())
                .roleCd(existing.getRoleCd())
                .email(existing.getEmail())
                .build();
    }    
    
    
    @Override
    public AuthInfo updateUserRoleProcess(UserInfoDTO dto) {
        Optional<UserInfoEntity> optEntity = userInfoRepository.findById(dto.getUserId());
        if (optEntity.isEmpty()) {
            return null;
        }

        UserInfoEntity entity = optEntity.get();
        entity.setRoleCd(dto.getRoleCd()); 			// 역할만 수정
        entity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        userInfoRepository.save(entity);

        return AuthInfo.builder()
                .userId(entity.getUserId())
                .userName(entity.getUserName())
                .roleCd(entity.getRoleCd())
                .build();
    }
    

    // ✅ 회원 삭제
    @Transactional
    @Override
    public void deleteUser(String userId) {
        if (!userInfoRepository.existsById(userId)) {
            throw new RuntimeException("삭제할 사용자가 존재하지 않습니다.");
        }
        userInfoRepository.deleteById(userId);
        log.info("사용자 [{}] 삭제 완료", userId);
    }

    // ✅ 사용자 수 카운트
    @Transactional
    @Override
    public long countUsers() {
        return userInfoRepository.count();
    }

    // ✅ Optional<UserInfoEntity> 조회 (refresh token 검증용)
    @Transactional
    @Override
    public Optional<UserInfoEntity> findById(String userId) {
        return userInfoRepository.findById(userId);
    }
}
