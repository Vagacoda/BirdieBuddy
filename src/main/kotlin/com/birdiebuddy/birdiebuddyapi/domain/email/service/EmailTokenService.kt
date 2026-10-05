package com.birdiebuddy.birdiebuddyapi.domain.email.service

import com.birdiebuddy.birdiebuddyapi.domain.email.EmailTokenEntity
import com.birdiebuddy.birdiebuddyapi.domain.email.EmailTokenRepository
// import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity 회원객체를 사용하지 않기 때문에 삭제
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.LocalDateTime

@Service
@Transactional
class EmailTokenService(
    private val emailTokenRepository: EmailTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService // Spring Boot가 EmailService 객체를 주입, 이메일 발송 서비스도 사용
) {
    fun createEmailVerification(email: String){
        // 회원가입 전에는 User가 없으므로 이메일 문자열만 받음
        // 회원 객체 하나를 받아 이메일 인증용 토큰을 만들고, 발송할 6자리 인증번호 원문을 반환
        val code = generateCode()
        // 000000부터 999999까지의 6자리 인증번호를 생성

        val tokenHash = passwordEncoder.encode(code)
            ?: throw IllegalStateException("인증번호 해시 생성에 실패하였습니다.")
        // 인증번호 원문을 BCrypt 해시값으로 변환

        val emailToken = EmailTokenEntity(
            user = null, // 저장된 유저정보가 없기 때문
            targetEmail = email, // 함수가 받은 이메일을 인증 대상 이메일로 저장
            tokenHash = tokenHash,
            expiresAt = LocalDateTime.now().plusMinutes(5)
        )

        emailTokenRepository.save(emailToken)
        // 인증번호의 해시값, 수신 이메일, 만료 시각을 EmailToken 테이블에 저장

        emailService.sendVerificationCode(
            to = email, // 함수가 받은 이메일 주소로 인증번호 발송
            code = code
        )
        // SES를 통해 회원 이메일로 발송

    }
    private fun generateCode(): String {
        val number = SecureRandom().nextInt(1_000_000)

        return "%06d".format(number)
        // SecureRandom으로 숫자를 만들고 %06d로 빈자리를 0으로 채움

    }
}