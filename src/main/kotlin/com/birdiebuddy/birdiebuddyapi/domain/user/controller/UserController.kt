package com.birdiebuddy.birdiebuddyapi.domain.user.controller

import com.birdiebuddy.birdiebuddyapi.domain.user.MyInfoResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.NicknameUpdateRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping("/me")
    fun getMyInfo(
        @AuthenticationPrincipal userId: Long
    ): ResponseEntity<MyInfoResponse> {
        return ResponseEntity.ok(
            userService.getMyInfo(userId)
        )
    }

    // 로그인한 본인의 닉네임 변경
    @PatchMapping("/me/nickname")
    fun updateNickname(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: NicknameUpdateRequest
    ): ResponseEntity<MyInfoResponse> {
        val result = userService.updateNickname(
            userId = userId,
            request = request
        )

        return ResponseEntity.ok(result)
    }
}