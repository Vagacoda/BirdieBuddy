package com.birdiebuddy.birdiebuddyapi.domain.email.service

import com.birdiebuddy.birdiebuddyapi.domain.email.EmailTokenEntity
import com.birdiebuddy.birdiebuddyapi.domain.email.EmailTokenRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
@Transactional
class EmailTokenService(
    private val emailTokenRepository: EmailTokenRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun createEmailVerification(user:UserEntity): String {
        // 회원 객체 하나를 받아 이메일 인증용 토큰을 만들고, 발송할 6자리 인증번호 원문을 반환
        val code = generateCode()
        // 000000부터 999999까지의 6자리 인증번호를 생성

        val tokenHash = passwordEncoder.encode(code)
            ?: throw IllegalStateException("인증번호 해시 생성에 실패하였습니다.")
        // 인증번호 원문을 BCrypt 해시값으로 변환

        val emailToken = EmailTokenEntity(
            user = user, // 어느 회원의 인증번호인지 연결
            targetEmail = user.email, // 인증번호 보낼 이메일
            tokenHash = tokenHash, // 해시처리된 인증번호
            expiresAt = LocalDateTime.now().plusMinutes(5) // 5분뒤 만료
        )

        emailTokenRepository.save(emailToken)
        // 저장

        return code
        // DB에 저장하지 않은 원문 인증번호를 반환
    }
    private fun generateCode(): String {
        val number = SecureRandom().nextInt(1_000_000)

        return "%06d".format(number)
        // SecureRandom으로 숫자를 만들고 %06d로 빈자리를 0으로 채움

    }
}