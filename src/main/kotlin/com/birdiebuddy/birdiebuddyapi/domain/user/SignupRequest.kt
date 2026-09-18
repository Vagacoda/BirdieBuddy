package com.birdiebuddy.birdiebuddyapi.domain.user
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

// 2026/09/18 - 회원가입 요청

data class SignupRequest(
    @field:NotBlank(message = "이메일은 필수 입력사항입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    @field:Size(max = 100, message = "이메일은 100자 이하로만 입력 가능합니다.")
    val email: String,
    // email의 제약사항을 정함

    @field:NotBlank(message = "닉네임은 필수 입력사항입니다.")
    @field:Size(max = 16, message = "닉네임은 16자 이하로 입력 가능합니다.")
    val nickname: String,
    // 별명의 제약사항을 정함

    @field:NotBlank(message = "비밀번호는 필수 입력사항입니다.")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*])[A-Za-z\\d!@#\$%^&*]{8,20}\$",
        message = "비밀번호는 8~20자이며 영문 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
    )
    val password: String
    // 비밀번호의 제약사항을 정함
)