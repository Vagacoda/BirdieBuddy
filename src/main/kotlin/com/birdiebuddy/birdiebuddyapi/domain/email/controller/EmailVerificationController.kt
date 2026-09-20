package com.birdiebuddy.birdiebuddyapi.domain.email.controller

import com.birdiebuddy.birdiebuddyapi.domain.email.EmailVerificationConfirmRequest
import com.birdiebuddy.birdiebuddyapi.domain.email.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController // HTTP요청 받고 JSON응답 보내는 API
@RequestMapping("/api/auth/email-verifications")
class EmailVerificationController (
    private val emailVerificationService: EmailVerificationService
    // Controller가 직접 DB에서 비교하지 않고 EmailVerificationService에게 맡김
){
    @PostMapping("/confirm")
    // POST /api/auth/email-verifications/confirm 요청시 confirm() 함수를 실행.
    fun confirm(
        @Valid @RequestBody request : EmailVerificationConfirmRequest
        //이메일 형식과 인증번호 6자리 규칙을 검사
        // EmailVerificationConfirmRequest가 토큰조회, 만료검사, 해시값 비교 등 수행
    ): ResponseEntity<Map<String, String>>{
        emailVerificationService.confirm(request)
        return ResponseEntity.ok(
            mapOf("message" to "이메일 인증이 완료되었습니다.")
            // HTTP200과 JSON을 보냄
        )
    }
}