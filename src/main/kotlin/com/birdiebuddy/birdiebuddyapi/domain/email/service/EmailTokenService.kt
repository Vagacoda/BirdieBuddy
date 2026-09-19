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
        val code = generateCode()

        val tokenHash = passwordEncoder.encode(code)
            ?: throw IllegalArgumentException("인증번호 해시 생성에 실패하였습니다.")

        val emailToken = EmailTokenEntity(
            user = user,
            targetEmail = user.email,
            tokenHash = tokenHash,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )

        emailTokenRepository.save(emailToken)

        return code
    }
    private fun generateCode(): String {
        val number = SecureRandom().nextInt(1_000_000)

        return "%06d".format(number)


    }
}