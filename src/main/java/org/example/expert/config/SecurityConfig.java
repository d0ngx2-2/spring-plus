package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    //등록 대상
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())  // 세션사용하지 않기 때문에 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않음.
                .authorizeHttpRequests(auth -> auth // 인가 정의 시작
                        .requestMatchers("/auth/**").permitAll() // 해당 경로는 인증 없이 허용
                        .requestMatchers("/admin/**").hasRole("ADMIN") // 해당경로는 ADMIN 권한이 있는 자만 허용
                        .anyRequest().authenticated() // 그 외는 모두 인증 해야함.
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // 폼 로그인 보다 먼저 처리.

        return httpSecurity.build();
    }
}
