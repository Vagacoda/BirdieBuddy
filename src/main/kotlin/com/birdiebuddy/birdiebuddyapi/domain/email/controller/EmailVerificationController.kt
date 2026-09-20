package com.birdiebuddy.birdiebuddyapi.domain.email.controller

import com.birdiebuddy.birdiebuddyapi.domain.email.EmailVerificationConfirmRequest
import com.birdiebuddy.birdiebuddyapi.domain.email.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/email-verification")
class EmailVerificationController (
    private val emailVerificationService: EmailVerificationService
){
    @PostMapping("/confirm")
    fun confirm(
        @Valid @RequestBody request : EmailVerificationConfirmRequest
    ): ResponseEntity<Map<String, String>>{
        emailVerificationService.confirm(request)
        return ResponseEntity.ok(
            mapOf("message" to "이메일 인증이 완료되었습니다.")
        )
    }
}