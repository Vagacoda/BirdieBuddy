package com.birdiebuddy.birdiebuddyapi.domain.email

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 2026/10/02 - 14:28
data class EmailVerificationSendRequest(
    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:Size(max = 254, message = "이메일은 254자 이하여야 합니다.")
    val email: String
    //{
    //  "email": "test@example.com"
    //}
    // 의 형태로 Json형식이 전송
)
