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
        // SpringSecurity에 접근규칙 생성, SpringBoot에 등록. HttpSecurity를 http변수명으로 사용
        http
            .csrf { it.disable() }
            // csrf보호기능 off, 추후 JWT(JsonWebToken)을 Authorization헤더에 넣어 보내는 API방식 사용
            // 로그인한 사용자가 요청할때마다 로그인상태를 증명해야하기 때문에 번거롭지 않게 JWT를 사용.
            .authorizeHttpRequests { auth -> // 주소별 접근 권한 설정이며, API 접근 권한을 정함
                auth.requestMatchers("/api/users").permitAll()
                // /api/users(회원가입 API주소) 는 비회원도 접근 가능.
                auth.anyRequest().authenticated()
                // 그 외 모든 주소는 JWT 등으로 로그인 인증을 통과한 회원만 접근가능.
            }
        // http.csrf{}
        // http.authorizeHttpRequests{}
        return http.build()
    }
}