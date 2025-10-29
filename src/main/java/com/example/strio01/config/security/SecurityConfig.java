package com.example.strio01.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import com.example.strio01.user.repository.UserInfoRepository;
import com.example.strio01.user.service.AuthService;
import com.example.strio01.config.jwt.JwtAuthenticationFilter;
import com.example.strio01.config.jwt.JwtAuthorizationFilter;
import com.example.strio01.config.jwt.JwtTokenProvider;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired private UserInfoRepository adminRepository;
    @Autowired @Qualifier("customCorsSource") private CorsConfigurationSource corsSource;
    @Autowired private AuthenticationConfiguration authenticationConfiguration;
    @Autowired private AuthService authService;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = authenticationManagerBean();

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsSource));
        http.formLogin(AbstractHttpConfigurer::disable);
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(authz -> authz
            // 🔴 1️⃣ 무조건 최상단에 — /api/** 전체를 완전 개방
            .anyRequest().permitAll()
        );

        // 🔴 2️⃣ 나머지 필터들은 그대로 두기 (disable하지 않음)
        JwtAuthenticationFilter jwtAuthFilter =
            new JwtAuthenticationFilter(authenticationManager, authService, jwtTokenProvider);
        JwtAuthorizationFilter jwtAuthorizationFilter =
            new JwtAuthorizationFilter(authenticationManager, adminRepository);

        http.addFilter(jwtAuthFilter);
        http.addFilter(jwtAuthorizationFilter);

        // 🔴 3️⃣ 디버깅용 (어떤 필터가 잡는지 로그 찍기)
        http.exceptionHandling(ex -> ex
            .accessDeniedHandler((req, res, e) -> {
                res.setStatus(403);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"error\":\"access denied\",\"msg\":\"" + e.getMessage() + "\"}");
            })
            .authenticationEntryPoint((req, res, e) -> {
                res.setStatus(401);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"error\":\"unauthorized\",\"msg\":\"" + e.getMessage() + "\"}");
            })
        );

        return http.build();
    }
}
