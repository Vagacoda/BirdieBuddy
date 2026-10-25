package com.birdiebuddy.birdiebuddyapi.domain.comment

import jakarta.validation.constraints.NotBlank

// 2026/10/25 - 댓글수정

data class CommentUpdateRequest(
    @field:NotBlank(message = "댓글 내용을 입력해주세요.")
    val content: String
)