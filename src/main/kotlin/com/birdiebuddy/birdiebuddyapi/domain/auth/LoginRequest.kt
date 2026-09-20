package com.birdiebuddy.birdiebuddyapi.domain.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import jakarta.validation.constraints.Email

// 2026/09/20 - 16:35 로그인서비스

data class LoginRequest (
    @field:NotBlank(message = "이메일은 필수 입력사항입니다.")
    @field:Size(max = 254, message = "입력가능한 이메일 길이가 아닙니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")

    val email: String,

    @field:NotBlank(message = "비밀번호는 필수 입력사항입니다.")
    val password: String,
)