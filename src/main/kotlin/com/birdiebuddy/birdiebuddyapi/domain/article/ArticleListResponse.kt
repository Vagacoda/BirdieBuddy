package com.birdiebuddy.birdiebuddyapi.domain.article

import java.time.LocalDateTime

// 2026/10/09 - 21:27
// 게시글 목록

data class ArticleListResponse(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val authorNickname: String,
    val viewCount: Int,
    val createdAt: LocalDateTime
    // 추후 추천수, 댓글수도 추가 예정
)