package com.birdiebuddy.birdiebuddyapi.domain.auth.service

import com.birdiebuddy.birdiebuddyapi.domain.auth.LoginRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@Transactional
class LoginService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    // 2025/10/06 - JWT추가
    private val jwtService: JwtService
    // LoginService가 JWT를 만들 수 있도록 JwtService를 받음
    // Spring이 JwtService 객체를 자동으로 넣음
){
    fun login(request: LoginRequest): String{
        // 2026/10/06
        // Long에서 String으로 변환
        // Long 타입인 회원 ID를 반환, 현재는 JWT의 긴 문자열이므로 String으로 변환
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")

        if (user.emailVerifiedAt == null){
            throw IllegalArgumentException("이메일 인증이 완룓히지 않았습니다.")
        }

        if(user.status != "ACTIVE"){
            throw IllegalArgumentException("로그인할 수 없는 계정입니다.")
        }

        if(!passwordEncoder.matches(request.password, user.passwordHash)){
            throw IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다.")
        }

        // 2026/10/06 - 20:33
        // 기존 로그인은 성공하면 회원 ID만 반환
        // JWT 방식에서는 클라이언트가 이후 요청에서 사용할 Access Token이 필요
        val userId = user.id
            ?: throw IllegalStateException("회원 ID가 존재하지 않습니다.")
        // DB에서 찾은 회원의 ID를 꺼냄, 없으면 예외처라

        return jwtService.createAccessToken(userId)
        // 회원 ID를 JWT 안의 subject에 넣음
    }
}