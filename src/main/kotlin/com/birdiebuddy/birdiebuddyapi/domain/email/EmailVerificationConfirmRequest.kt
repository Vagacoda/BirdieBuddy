package com.birdiebuddy.birdiebuddyapi.domain.email

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class EmailVerificationConfirmRequest (
    @field:NotBlank(message = "이메일은 필수 입력사항입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:Size(max = 254, message = "이메일 입력 글자수의 범위를 벗어났습니다.")
    val email: String,

    @field:NotBlank(message = "인증번호는 필수 입력사항입니다.")
    @field:Pattern(
        regexp = """^\d{6}$""",
        // raw string문법
        // ^ : 문자열 시작
        // \d : 숫자 한 자리
        // {6} : 숫자가 6개
        // $ : 문자열 끝
        message = "인증번호는 숫자 6자리 입니다."
    )
    val code: String
)