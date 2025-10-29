package com.example.strio01.config.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;

@Configuration
public class CorsConfig {

    @Bean("customCorsSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 개발환경: Origin 명시 (withCredentials 사용 시 와일드카드 금지 규칙 회피)
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001",
            "http://192.168.0.4:3000"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
            "Authorization",
            "Authorization-refresh",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        config.setExposedHeaders(List.of(
            "Authorization",
            "Authorization-refresh",
            "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 전 경로 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
