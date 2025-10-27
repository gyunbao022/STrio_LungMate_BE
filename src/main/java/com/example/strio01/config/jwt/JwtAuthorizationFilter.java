package com.example.strio01.config.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.strio01.config.auth.PrincipalDetails;
import com.example.strio01.user.dto.AuthInfo;
import com.example.strio01.user.entity.UserInfoEntity;
import com.example.strio01.user.repository.UserInfoRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private UserInfoRepository adminRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserInfoRepository adminRepository) {
        super(authenticationManager);
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        log.info("인가가 필요한 주소 요청이 실행되는 메소드: doFilterInternal()");
        System.out.println("==============================::7:::1");   

        // 1. 인가가 필요한 요청이 전달된다.
        String accessToken = request.getHeader("Authorization");
        log.info("Authorization: {}", accessToken);
        System.out.println("==============================::7:::2");

        // 2. Header 확인
        if (accessToken == null || !accessToken.startsWith("Bearer")) {
            System.out.println("==============================::7:::2 1===");
            chain.doFilter(request, response);
            return;
        }

        System.out.println("==============================::7:::3");
        // 3. JWT 토큰을 검증해서 정상적인 사용자인지, 권한이 맞는지 확인
        String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
        System.out.println("==============================::7:::4");
        
        try {
            // JWT 토큰 검증 및 클레임 추출
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("mySecurityCos"))
                    .build()
                    .verify(jwtToken);
            
            // ⚠️ 중요: 클레임 이름을 'userId'로 수정 (기존 'adminId'에서 변경)
            String username = decodedJWT.getClaim("userId").asString();
            log.info("username=>{}", username);
            
            System.out.println("==============================::7:::5");

            // 서명이 정상적으로 처리되었으면
            if (username != null) { 
                // DB에서 사용자 정보 조회
                Optional<UserInfoEntity> optMembersEntity = adminRepository.findById(username);
                
                if (optMembersEntity.isEmpty()) {
                    log.warn("사용자를 찾을 수 없음: {}", username);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"user not found\"}");
                    return;
                }
                
                UserInfoEntity adminEntity = optMembersEntity.get();
                log.info("************{}", adminEntity.getUserId());
                
                // roleCd를 ROLE_* 형태로 변환하여 권한 생성
                String roleCd = adminEntity.getRoleCd();
                String roleName = mapRoleCdToRoleName(roleCd);
                
                log.info("사용자 권한: roleCd={}, roleName={}", roleCd, roleName);
                
                // GrantedAuthority 리스트 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(roleName));
                
                AuthInfo authInfo = new AuthInfo(adminEntity.getUserId(), adminEntity.getPasswd(), adminEntity.getUserName());
                PrincipalDetails principalDetails = new PrincipalDetails(authInfo);

                // Authentication 객체 생성 시 authorities 포함
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        principalDetails, 
                        null,
                        authorities  // DB에서 조회한 roleCd 기반 권한 설정
                );

                log.info("authentication.getName() = {}", authentication.getName());
                log.info("authentication.authorities = {}", authentication.getAuthorities());
                PrincipalDetails prin = (PrincipalDetails) (authentication.getPrincipal());
                log.info("authentication.principal.getUsername()={}", prin.getUsername());
                
                // 강제로 시큐리티의 세션에 접근하여 값 저장한다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);
            
        } catch (TokenExpiredException e) {
            log.warn("⚠️ AccessToken 만료됨: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"accessToken expired\"}");

        } catch (Exception e) {
            log.error("❌ JWT 처리 중 예외 발생", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid token\"}");
        }
    }
    
    /**
     * roleCd를 Spring Security의 ROLE_* 형식으로 변환
     * @param roleCd A, D, X 등
     * @return ROLE_ADMIN, ROLE_DOCTOR, ROLE_XRAY_OPERATOR 등
     */
    private String mapRoleCdToRoleName(String roleCd) {
        if (roleCd == null) {
            return "ROLE_USER";
        }
        
        return switch (roleCd) {
            case "A" -> "ROLE_ADMIN";
            case "D" -> "ROLE_DOCTOR";
            case "X" -> "ROLE_XRAY";
            default -> "ROLE_USER";
        };
    }

}// end class