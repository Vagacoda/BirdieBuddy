package com.birdiebuddy.birdiebuddyapi.domain.comment

import java.time.LocalDateTime

// 2026/10/11 - 18:39
// 클라이언트에게 댓글 정보를 돌려줄 데이터
// CommentCreateRequest
// → 사용자가 보낼 값
// → content만 필요
//
// CommentResponse
// → 서버가 돌려줄 값
// → 댓글 ID, 내용, 작성자, 작성·수정 시각 포함

data class CommentResponse(
    val id: Long,
    val content: String,
    val authorNickname: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)