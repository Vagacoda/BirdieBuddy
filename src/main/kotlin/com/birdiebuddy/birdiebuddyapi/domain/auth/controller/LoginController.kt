package com.birdiebuddy.birdiebuddyapi.domain.auth.controller

import com.birdiebuddy.birdiebuddyapi.domain.auth.LoginRequest
import com.birdiebuddy.birdiebuddyapi.domain.auth.service.LoginService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController // HTTP 요청 받고 JSON응답 보내는 API
@RequestMapping("/api/auth")
class LoginController (
    private val loginService: LoginService
){
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
        // 클라이언트가 보낸 JSON을 LoginRequest 객체로 바꾸고 검증
    ): ResponseEntity<Map<String, String>> {
        // 2026/10/07 - 20:37 JWT추가로 인한 주석처리 -----------------------------------
        //val userId = loginService.login(request)
        // 해당 유저가 이메일이 맞는지, 이메일 인증을 완료 했는지 ACTIVE상태인지, 해시값 일치 여부
        // ---------------------------------------------------------------------------
        val accessToken = loginService.login(request)
        // LoginService가 만든 JWT 문자열을 받아서 accessToken이라는 변수에 저장

        return ResponseEntity.ok(
            mapOf(
//                "id" to userId,
//                "message" to "로그인이 성공했습니다."
                "accessToken" to accessToken,
                "tokenType" to "Bearer",
                // Authorization: Bearer {토큰} 형식으로 보냄
                // Bearer: 들고 있는 사람, 소지자이며 JWT에서는 들고 있는 사람, 소지자라는 의미로 사용
                "message" to "로그인이 성공했습니다."
            )
        )
    }
}