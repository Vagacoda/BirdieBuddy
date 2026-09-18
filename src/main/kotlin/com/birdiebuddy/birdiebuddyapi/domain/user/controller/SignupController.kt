package com.birdiebuddy.birdiebuddyapi.domain.user.controller

import com.birdiebuddy.birdiebuddyapi.domain.user.SignupRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.service.SignupService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController // HTTP 요청을 받고 JSON 응답을 보내는 API 담당
@RequestMapping("/api/user") // 컨트롤러의 기본 주소
class SignupController (
    private val signupService: SignupService
){
    @PostMapping // POST요청이 오면 signup()함수 실행
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<Map<String, Any>> {
        // @Valid: SignupRequest의 이메일 형식, 닉네임 길이, 비밀번호 규칙을 검사
        // @RequestBody: 응답 본문뿐 아니라 HTTP 상태 코드도 함께 정해서 보낼 수 있는 응답 객체
        val userId = signupService.signup(request) // Controller는 직접 DB를 건들지 않음 SignupService가 역할 수행
        // 컨트롤러가 받은 회원가입 정보를 서비스로 넘김, 서비스는 중복 검사 -> MEMBER 권한 조회 -> 비밀번호 해시 처리 → DB 저장을 수행

        return ResponseEntity.ok( // HTTP 응답을 만들어 보냄
            mapOf(
                "id" to userId,
                "message" to "회원가입이 완료되었습니다"
            )
        )
    }
}