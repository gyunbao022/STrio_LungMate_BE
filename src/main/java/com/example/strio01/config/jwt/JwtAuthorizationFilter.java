package com.example.strio01.config.jwt;

import java.io.IOException;
import java.util.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
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

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserInfoRepository adminRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserInfoRepository adminRepository) {
        super(authenticationManager);
        this.adminRepository = adminRepository;
    }

    private boolean isApiPath(HttpServletRequest request) {
        String uri = request.getRequestURI();        // e.g. /api/analyze/by-id
        String servletPath = request.getServletPath();// 대부분 동일하지만 환경에 따라 다를 수 있음
        return (uri != null && uri.startsWith("/api/")) || (servletPath != null && servletPath.startsWith("/api/"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) return true; // preflight 무조건 통과
        if (path.startsWith("/api/analyze")) return true;    // ✅ 분석 API 우회
        if (path.startsWith("/api/diagnoses")) return true;  // ✅ 대안 경로 우회

        return false;
    }


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	    throws IOException, ServletException {
	
	    String accessToken = request.getHeader("Authorization");
	    if (accessToken == null || !accessToken.startsWith("Bearer ")) {
	        // ✅ 토큰 없으면 그대로 다음 필터 (접근제어는 SecurityConfig의 permitAll이 처리)
	        chain.doFilter(request, response);
	        return;
	    }

        try {
            String jwtToken = accessToken.replace("Bearer ", "");
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512("mySecurityCos")).build().verify(jwtToken);
            String username = decodedJWT.getClaim("userId").asString();

            if (username != null) {
                Optional<UserInfoEntity> opt = adminRepository.findById(username);
                if (opt.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"user not found\"}");
                    return;
                }

                UserInfoEntity entity = opt.get();
                String role = mapRoleCd(entity.getRoleCd());
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                AuthInfo authInfo = new AuthInfo(entity.getUserId(), entity.getPasswd(), entity.getUserName());
                authInfo.setRoleCd(entity.getRoleCd()); 
                PrincipalDetails principal = new PrincipalDetails(authInfo);

                Authentication authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"token expired\"}");
        } catch (Exception e) {
            log.error("JWT error", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"invalid token\"}");
        }
    }

    private String mapRoleCd(String cd) {
        return switch (cd) {
            case "A" -> "ROLE_ADMIN";
            case "D" -> "ROLE_DOCTOR";
            case "X" -> "ROLE_XRAY_OPERATOR";
            default -> "ROLE_USER";
        };
    }
}
