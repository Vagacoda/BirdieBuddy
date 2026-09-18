package com.birdiebuddy.birdiebuddyapi.domain.user.service

import com.birdiebuddy.birdiebuddyapi.domain.role.RoleRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.SignupRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

// 2026/09/18 - 회원가입 요청을 받아서 검증한 뒤, 비밀번호를 해시 처리하고 User 테이블에 저장

@Service // 회원가입 관련 로직을 처리하는 Spring서비스라고 등록
@Transactional // 트랜잭션, 중간에 오류나면 중간작업 취소.
class SignupService (
    private val userRepository: UserRepository, // User테이블을 조회, 저장
    private val roleRepository: RoleRepository, // Role테이블에서 Member권한 찾음
    private val passwordEncoder: PasswordEncoder // 입력된 비밀번호를 해시값으로 변경
){
    fun signup(request: SignupRequest): Long {
        // 컨트롤러가 받은 SignupRequest를 전달받아 회원가입을 수행, 완료된 회원의 ID를 반환

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