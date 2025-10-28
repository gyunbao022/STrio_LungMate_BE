package com.example.strio01.user.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.strio01.config.auth.PrincipalDetails;
import com.example.strio01.config.jwt.JwtTokenProvider;
import com.example.strio01.user.controller.UserInfoController.ResetPasswordRequest;
import com.example.strio01.user.dto.AuthInfo;
import com.example.strio01.user.dto.UserInfoDTO;
import com.example.strio01.user.entity.UserInfoEntity;
import com.example.strio01.user.service.AuthService;
import com.example.strio01.user.service.MailService;
import com.example.strio01.user.service.PasswordResetService;
import com.example.strio01.user.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin("*")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordResetService passwordResetService;    

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private MailService mailService;       

    public UserInfoController() {}

    
    // http://localhost:8090/league/list
    @GetMapping(value="/member/list")
    public ResponseEntity<Map<String, Object>> listExecute(){
        Map<String, Object> map = new HashMap<>();
   		map.put("merberList", userInfoService.listAllUsers());

    	return ResponseEntity.ok().body(map);
    }//end listExecute()//////    
    
    // 회원가입
    @PostMapping("/member/signup")
    public ResponseEntity<AuthInfo> signup(@RequestBody UserInfoDTO dto) {
    	//dto.setPasswd(passwordEncoder.encode(dto.getPasswd())); ////// 2025.10.22 jaemin AutoInfo 에서 암호화 처리하므로 여기서는 제외한다. 
        AuthInfo authInfo = userInfoService.createUserProcess(dto);
        return ResponseEntity.ok(authInfo);
    }
   

    // 단일 사용자 조회
    @GetMapping("/member/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable("userId") String userId,
                                               @AuthenticationPrincipal PrincipalDetails principal) {
        log.info("조회 요청 userId={}, principal={}", userId, principal.getUsername());
        UserInfoDTO userDTO = userInfoService.getUser(userId);
        return ResponseEntity.ok(userDTO);
    }

    // 회원 정보 수정
    @PutMapping("/member/update")
    public ResponseEntity<AuthInfo> updateUser(@RequestBody UserInfoDTO dto) {
        //dto.setPasswd(passwordEncoder.encode(dto.getPasswd()));
        AuthInfo authInfo = userInfoService.updateUserProcess(dto);
        return ResponseEntity.ok(authInfo);
    }
    
    // 회원 정보 수정
    @PutMapping("/member/updatePasswd")
    public ResponseEntity<AuthInfo> updatePasswd(@RequestBody UserInfoDTO dto) {
        //dto.setPasswd(passwordEncoder.encode(dto.getPasswd()));
        AuthInfo authInfo = userInfoService.updateUserPasswd(dto);
        return ResponseEntity.ok(authInfo);
    }    
    
    // 회원 롤정보 수정 (2025.10.22)
    @PutMapping("/member/updateRole")
    public ResponseEntity<AuthInfo> updateUserRole(@RequestBody UserInfoDTO dto) {
        log.info("===> [회원 역할 수정 요청] userId={}, roleCd={}", dto.getUserId(), dto.getRoleCd());

        // ✅ 비밀번호 암호화 제거: 역할만 수정
        AuthInfo authInfo = userInfoService.updateUserRoleProcess(dto);

        if (authInfo != null) {
            log.info("===> [회원 역할 수정 완료] userId={}, newRole={}", dto.getUserId(), dto.getRoleCd());
            return ResponseEntity.ok(authInfo);
        } else {
            log.warn("===> [회원 역할 수정 실패] userId={}", dto.getUserId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(AuthInfo.builder()
                            .userId(dto.getUserId())
                            .roleCd(dto.getRoleCd())
                            .build());
        }
    }
 
    
    // 회원 삭제 (2025.10.21 완료)
    @DeleteMapping("/member/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        userInfoService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    // 로그아웃
    //PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/member/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization-refresh") String refreshToken) {
        String userId = JWT.require(Algorithm.HMAC512("mySecurityCos"))
                .build()
                .verify(refreshToken)
                .getClaim("userId")
                .asString();
        log.info("로그아웃 userId={}", userId);
        authService.deleteRefreshToken(userId);
        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }

    // 토큰 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("Authorization-refresh");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리프레시 토큰이 없습니다."));
        }
        try {
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            boolean isValid = authService.validateRefreshToken(userId, refreshToken);
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "유효하지 않은 리프레시 토큰입니다."));
            }

            UserInfoEntity entity = userInfoService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 없음"));

            AuthInfo authInfo = AuthInfo.builder()
                    .userId(entity.getUserId())
                    .userName(entity.getUserName())
                    .roleCd(entity.getRoleCd())
                    .build();

            String newAccessToken = jwtTokenProvider.createAccessToken(authInfo);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            response.setHeader("Authorization", "Bearer " + newAccessToken);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "토큰 검증 실패", "message", e.getMessage()));
        }
        
    }
    /**
     * 의사(roleCd='D') 목록 조회
     * - ADMIN 또는 XRAY_OPERATOR만 접근 가능
     */
    @GetMapping("/members/doctors")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY_OPERATOR')")
    public ResponseEntity<List<Map<String, String>>> getDoctors() {
        log.info("===> [의사 목록 조회 요청]");
        
        List<UserInfoDTO> doctors = userInfoService.findUsersByRole("D");
        
        // 간단한 응답 형태로 변환 (memberId, memberName만)
        List<Map<String, String>> result = doctors.stream()
            .map(dto -> {
                Map<String, String> doctor = new HashMap<>();
                doctor.put("memberId", dto.getUserId());
                doctor.put("memberName", dto.getUserName());
                return doctor;
            })
            .collect(Collectors.toList());
        
        log.info("===> [의사 목록 조회 완료] count={}", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 의사 검색 (선택적 - 이름/ID 부분일치)
     */
    @GetMapping("/members/doctors/search")
    @PreAuthorize("hasAnyRole('ADMIN','XRAY_OPERATOR')")
    public ResponseEntity<List<Map<String, String>>> searchDoctors(
            @RequestParam(required = false, defaultValue = "") String q) {
        log.info("===> [의사 검색 요청] query={}", q);
        
        List<UserInfoDTO> doctors;
        if (q == null || q.trim().isEmpty()) {
            // 빈 검색어면 전체 의사 목록 반환
            doctors = userInfoService.findUsersByRole("D");
        } else {
            // 검색어가 있으면 필터링
            doctors = userInfoService.searchDoctors(q);
        }
        
        List<Map<String, String>> result = doctors.stream()
            .map(dto -> {
                Map<String, String> doctor = new HashMap<>();
                doctor.put("memberId", dto.getUserId());
                doctor.put("memberName", dto.getUserName());
                return doctor;
            })
            .collect(Collectors.toList());
        
        log.info("===> [의사 검색 완료] query={}, count={}", q, result.size());
        return ResponseEntity.ok(result);
    }
    
    // 아이디 찾기
    @PostMapping("/member/findId")
    public ResponseEntity<?> getUserId(@RequestBody UserInfoDTO dto) {
        UserInfoDTO userDTO = userInfoService.getUserId(dto);

        if (userDTO == null) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("message", "입력하신 이름과 이메일로 일치하는 회원이 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        }

        return ResponseEntity.ok(userDTO);
    }   

    // 비밀번호 수정링크
    @PostMapping("/member/findPasswd")
    public ResponseEntity<?> getPasswd(@RequestBody UserInfoDTO dto) {
        UserInfoDTO userDTO = userInfoService.getUserInfo(dto);

        if (userDTO == null) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("message", "입력하신 아이디와 이메일로 일치하는 회원이 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        }

        // 비밀번호 재설정 링크 생성
        //String resetToken = java.util.UUID.randomUUID().toString();
        //String resetLink = "http://localhost:3000/reset-password?token=" + resetToken + "&userId=" + userDTO.getUserId();
        String token = passwordResetService.createResetToken(userDTO.getUserId());
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        // 메일 전송   
        mailService.sendPasswordResetMail(userDTO.getEmail(), resetLink);

        Map<String, Object> successBody = new HashMap<>();
        successBody.put("message", "비밀번호 재설정 링크가 이메일로 발송되었습니다.");
        successBody.put("resetLink", resetLink); // 디버깅용 (실제 서비스에서는 제거)
        return ResponseEntity.ok(successBody);
    }  
    
    @Data
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    @PostMapping("/member/resetPasswd")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            passwordResetService.resetPassword(req.getToken(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }  
    
}
