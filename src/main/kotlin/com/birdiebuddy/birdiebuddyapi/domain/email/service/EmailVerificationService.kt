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

        if (!emailToken.expiresAt.isAfter(now)){
            throw IllegalArgumentException("인증번호가 만료되었습니다.")
        }
        if (!passwordEncoder.matches(request.code, emailToken.tokenHash)){
            throw IllegalArgumentException("인증번호가 일치하지 않습니다.")
        }
        val user = emailToken.user
            ?: throw IllegalArgumentException("인증번호와 연결된 회원이 없습니다.")

        emailToken.usedAt = now
        user.emailVerifiedAt = now
    }
}