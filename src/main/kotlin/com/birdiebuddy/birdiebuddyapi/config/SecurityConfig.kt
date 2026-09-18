package com.birdiebuddy.birdiebuddyapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain

// 2026/09/18 -15:37 SpringBoot에 비밀번호 해시 전용도구 등록하는 파일

@Configuration // SpringSecurity 설정 클래스
class SecurityConfig { // 보안 설정 클래스

    @Bean // @Bean은 이 함수가 만든 객체를 SpringBoot가 관리하도록 함
    fun passwordEncoder(): PasswordEncoder{
        return BCryptPasswordEncoder() // Bcrypt 방식 비밀번호 처리도구를 새로 만들어 반환
    }

    // 어떤 APU에 누가 접근할 수 있는지 정하는 보안 규칙
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // SpringSecurity에 접근규칙 생성, SpringBoot에 등록
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/api/users").permitAll()
                auth.anyRequest().authenticated()
            }
        return http.build()
    }
}