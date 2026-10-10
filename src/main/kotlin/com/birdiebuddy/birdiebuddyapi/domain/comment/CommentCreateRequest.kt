package com.birdiebuddy.birdiebuddyapi.domain.comment

import jakarta.validation.constraints.NotBlank

// 2026/10/10 - 22:13
// 댓글 작성 요청값을 담음

data class CommentCreateRequest(
    @field:NotBlank(message = "댓글 내용을 입력해주세요.")
    val content: String
)