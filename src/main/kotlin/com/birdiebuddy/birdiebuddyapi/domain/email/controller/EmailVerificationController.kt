package com.birdiebuddy.birdiebuddyapi.domain.email.controller

import com.birdiebuddy.birdiebuddyapi.domain.email.EmailVerificationConfirmRequest
import com.birdiebuddy.birdiebuddyapi.domain.email.service.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
// 2026/10/02 - 15:09\
import com.birdiebuddy.birdiebuddyapi.domain.email.EmailVerificationSendRequest

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
            // mapOf : JSON 응답 본문을 만드는 코드
            //{
            //  "message": "이메일 인증이 완료되었습니다."
            //}
            // ResponseEntity.ok : ok가 HTTP 상태 코드 200 OK를 자동으로 설정.
            // HTTP200과 JSON을 보냄
        )
    }
    @PostMapping("/send")
    fun sendVerificationCode(
        @Valid @RequestBody request: EmailVerificationSendRequest
        // JSON을 EmailVerificationSendRequest 객체로 받음
        // @RequestBody: JSON 본문을 Kotlin 객체로 변환
        // @Valid: 이메일이 비었는지, 형식이 맞는지 검사
        // 검사 실패 시 400 응답
    ): ResponseEntity<Map<String, String>> {
        // HTTP 상태 코드(ok이므로 200반환)
        // JSON 본문이 “문자열 이름 : 문자열 값” 형태
        emailVerificationService.sendSignupVerification(request.email)
        // 요청에서 받은 이메일만 꺼내 서비스에 넘김
        // 서비스가 중복 이메일인지 다시 확인, 통과하면 인증번호 생성·DB 저장·SES 발송을 처리

        return ResponseEntity.ok(
            mapOf(
                "message" to "인증번호를 이메일로 발송했습니다."
            )
        )
        // 발송이 성공했을 때 HTTP 200과 JSON반환
    }
}