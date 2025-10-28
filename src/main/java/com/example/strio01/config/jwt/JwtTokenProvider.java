package com.example.strio01.config.jwt;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.strio01.user.dto.AuthInfo;

import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
   private final String secretKey = "mySecurityCos";
   // accessToken: 15분 유효
   public String createAccessToken( AuthInfo authInfo ) {
       return JWT.create()
               .withSubject("AccessToken")
               .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 15 )) //1분
               .withClaim("userId", authInfo.getUserId())
               .withClaim("roleCd", authInfo.getRoleCd()) // 추가
               .withClaim("authorities", List.of(mapRoleCdToRoleName(authInfo.getRoleCd())))
//             .withClaim("authRole", authInfo.getAuthRole().toString())
               .sign(Algorithm.HMAC512(secretKey));
   } 
   // refreshToken: 2주 유효
   public String createRefreshToken(String userId) {
       return JWT.create()
               .withSubject("RefreshToken")
               .withExpiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 14)) //2주
               .withClaim("userId", userId)
               .sign(Algorithm.HMAC512(secretKey));
   }
   public String getUserIdFromToken(String token) {
       return JWT.require(Algorithm.HMAC512(secretKey))
               .build()
               .verify(token)
               .getClaim("userId")
               .asString();
   }   
   public String getEmailFromToken(String token) {
       return JWT.require(Algorithm.HMAC512(secretKey))
               .build()
               .verify(token)
               .getClaim("adminId")
               .asString();
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
           case "X" -> "ROLE_XRAY_OPERATOR";
           default -> "ROLE_USER";
       };
   }
}




