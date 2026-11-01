package com.birdiebuddy.birdiebuddyapi.domain.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PasswordUpdateRequest(
    @field:NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    @field:Size(
        max = 20,
        message = "현재 비밀번호는 20자 이하로 입력해 주세요."
    )
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호를 입력해 주세요.")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*])[A-Za-z\\d!@#\$%^&*]{8,20}\$",
        message = "비밀번호는 8~20자이며 영문 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
    )
    val newPassword: String
)