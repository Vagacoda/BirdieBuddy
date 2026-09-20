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
    private val passwordEncoder: PasswordEncoder
){
    fun login(request: LoginRequest): Long{
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

        return user.id
            ?: throw IllegalStateException("회원 ID가 존재하지 않습니다.")
    }
}