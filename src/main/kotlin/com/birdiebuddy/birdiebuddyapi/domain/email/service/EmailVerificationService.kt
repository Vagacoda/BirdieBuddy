package com.birdiebuddy.birdiebuddyapi.domain.email.service

import com.birdiebuddy.birdiebuddyapi.domain.email.EmailTokenRepository
import com.birdiebuddy.birdiebuddyapi.domain.email.EmailVerificationConfirmRequest
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

// 2026/09/20 - 15:12 인증번호가 유효한지 확인하고, 성공하면 인증 완료 상태로 바꾸는 클래스
@Service
@Transactional
class EmailVerificationService (
    private val emailTokenRepository: EmailTokenRepository,
    private val passwordEncoder: PasswordEncoder
){
    fun confirm(request : EmailVerificationConfirmRequest){
        val emailToken = emailTokenRepository
            .findFirstByTargetEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
                request.email,
                purpose = "EMAIL_VERIFY"
            )
            ?: throw IllegalArgumentException("현재 사용 가능한 인증번호가 없습니다.")

        val now = LocalDateTime.now()
        // 만료 검사와 인증 완료 기록에 같은 시각을 쓰기 위함

        if (!emailToken.expiresAt.isAfter(now)){
            throw IllegalArgumentException("인증번호가 만료되었습니다.")
        }
        // 만료시각이 현재 시각보다 뒤인지 확인
        if (!passwordEncoder.matches(request.code, emailToken.tokenHash)){
            throw IllegalArgumentException("인증번호가 일치하지 않습니다.")
        }
        // 사용자 입력 인증번호와 DB 해시값이 같은 원문에서 나온 것인지 확인. 다르면 인증 실패 출력
        val user = emailToken.user
            ?: throw IllegalArgumentException("인증번호와 연결된 회원이 없습니다.")
        // 인증번호와 연결된 회원을 가져옴
        emailToken.usedAt = now
        // 인증번호는 사용 완료 상태이며 같은번호로 다시 인증 불가함
        user.emailVerifiedAt = now
        // 회원의 이메일 인증 완료 시각을 기록함
    }
}