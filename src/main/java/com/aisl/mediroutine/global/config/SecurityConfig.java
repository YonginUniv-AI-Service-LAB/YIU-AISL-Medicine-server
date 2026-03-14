package com.aisl.mediroutine.global.config;

import com.aisl.mediroutine.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfig corsConfig;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/password/reset",
                                "/emails/verification-code/send",
                                "/emails/verification-code/verify"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // SecurityContext 저장 방식
                .securityContext(ctx -> ctx
                        .securityContextRepository(new HttpSessionSecurityContextRepository())
                )

                // 세션 설정
                .sessionManagement(session -> session
                        .maximumSessions(1)
                )

                // ⭐ 로그아웃 설정 (추가)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )

                // 예외 처리
                .exceptionHandling(ex -> ex

                        // 인증 안된 경우
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            response.getWriter().write(
                                    objectMapper.writeValueAsString(
                                            ApiResponse.failure("로그인이 필요합니다.")
                                    )
                            );
                        })

                        // 권한 없는 경우
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

                            response.getWriter().write(
                                    objectMapper.writeValueAsString(
                                            ApiResponse.failure("접근 권한이 없습니다.")
                                    )
                            );
                        })
                );

        return http.build();
    }
}