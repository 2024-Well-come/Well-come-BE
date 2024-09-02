package com.wellcome.WellcomeBE.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() {
        return new CustomAuthenticationFilter(tokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // csrf 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 사용 x
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/health-check").permitAll()
                            .requestMatchers("/api/oauth2/kakao").permitAll()
                            .requestMatchers("/api/oauth2/kakao/refresh").permitAll()
                            .requestMatchers("/api/oauth2/kakao/logout").permitAll()
                            .requestMatchers("/getTourBasicApiData").permitAll()
                            .requestMatchers("/place/details").permitAll()
                            .requestMatchers("/place/id").permitAll()
                            .anyRequest().authenticated();
                })

                // CustomAuthenticationFilter 추가
                .addFilterBefore(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}