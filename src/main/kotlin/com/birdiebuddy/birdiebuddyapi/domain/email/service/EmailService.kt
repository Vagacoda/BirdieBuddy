package com.birdiebuddy.birdiebuddyapi.domain.email.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

// 2026/09/27 EmailTokenService는 실제 이메일을 작서애서 SES SMTP서버로 보내는 서비스 클래스임.

@Service // 이메일 발송 기능을 담당
class EmailService(
    private val mailSender: JavaMailSender, // Spring Boot가 제공하는 메일 전송 도구

    @Value("\${app.mail.from}")
    private val fromEmail: String
    // application.yml값 가져옴
) {
    fun sendVerificationCode(to: String, code: String) {
        // to: 인증 메일을 받을 회원 이메일
        // code: 방금 생성한 6자리 인증번호 원문
        val message = SimpleMailMessage()
        // 메일 한 통을 담을 빈 객체

        message.setFrom(fromEmail) // 발신자
        message.setTo(to) // 수신자
        message.subject = "[BirideBuddy] 이메일 인증번호" // 제목
        message.text = """
            BirdieBuddy 이베일 인증번호 입니다.
            
            인증번호 : $code
            
            인증번호는 5분후 만료됩니다.
            """.trimIndent() // 본문

        mailSender.send(message)
    }
}