package com.birdiebuddy.birdiebuddyapi.domain.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NicknameUpdateRequest(
    // 회원가입 때와 동일하게 공백만 입력하는 것은 금지
    @field:NotBlank(message = "닉네임은 필수 입력사항입니다.")
    @field:Size(
        max = 16,
        message = "닉네임은 16자 이하로 입력 가능합니다."
    )
    val nickname: String
)