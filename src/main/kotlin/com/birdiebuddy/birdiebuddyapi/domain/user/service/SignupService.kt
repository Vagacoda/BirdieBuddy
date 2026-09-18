package com.birdiebuddy.birdiebuddyapi.domain.user.service

import com.birdiebuddy.birdiebuddyapi.domain.role.RoleRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.SignupRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

// 2026/09/18 - 회원가입 요청을 받아서 검증한 뒤, 비밀번호를 해시 처리하고 User 테이블에 저장

@Service
@Transactional
class SignupService (
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder
){
    fun signup(request: SignupRequest): Long {

        if (userRepository.existsByEmail(request.email)){
            throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
        }

        if (userRepository.existsByNickname(request.nickname)){
            throw IllegalArgumentException("중복된 닉네임입니다.")
        }

        val memberRole = roleRepository.findByName("MEMBER")
            ?: throw IllegalStateException("MEMBER권한이 존재하지 않습니다.")

        val passwordHash = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("비밀번호 해시 생성에 실패했습니다.")

        val user = UserEntity(
            role = memberRole,
            email = request.email,
            nickname = request.nickname,
            passwordHash = passwordHash
        )
        return userRepository.save(user).id!!
    }
}