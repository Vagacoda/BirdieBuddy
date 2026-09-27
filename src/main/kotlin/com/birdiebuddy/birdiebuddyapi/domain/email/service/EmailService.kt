package com.birdiebuddy.birdiebuddyapi.domain.email.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender,

    @Value("\${app.bailk.from}")
    private val fromEmail: String
) {
    fun sendVerificationCode(to: String, code: String) {
        val message = SimpleMailMessage()

        message.setFrom(fromEmail)
        message.setTo(to)
        message.subject = "[BirideBuddy] 이메일 인증번호"
        message.text = """
            BirdieBuddy 이베일 인증번호 입니다.
            
            인증번호 : $code
            
            인증번호는 5분후 만료됩니다.
            """.trimIndent()

        mailSender.send(message)
    }
}