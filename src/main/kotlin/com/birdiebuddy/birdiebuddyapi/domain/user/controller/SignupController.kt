package com.birdiebuddy.birdiebuddyapi.domain.user.controller

import com.birdiebuddy.birdiebuddyapi.domain.user.SignupRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.service.SignupService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user") // API요청 받는곳
class SignupController (
    private val signupService: SignupService
){
    @PostMapping // POST요청이 오면 signup()함수 실행
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<Map<String, Any>> {
        val userId = signupService.signup(request) // Controller는 직접 DB를 건들지 않음 SignupService가 역할 수행

        return ResponseEntity.ok(
            mapOf(
                "id" to userId,
                "message" to "회원가입이 완료되었습니다"
            )
        )
    }
}