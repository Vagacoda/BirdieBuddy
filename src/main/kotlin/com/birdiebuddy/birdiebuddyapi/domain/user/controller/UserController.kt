package com.birdiebuddy.birdiebuddyapi.domain.user.controller

import com.birdiebuddy.birdiebuddyapi.domain.user.MyInfoResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    // GET /api/users/me
    // 현재 로그인한 회원 자신의 정보를 조회
    @GetMapping("/me")
    fun getMyInfo(
        // JWT 필터에서 인증한 회원 ID를 받음
        // 사용자가 직접 입력한 ID를 사용하는 것이 아님
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<MyInfoResponse> {
        val result = userService.getMyInfo(userId)

        return ResponseEntity.ok(result)
    }
}