package com.birdiebuddy.birdiebuddyapi.domain.article

import java.time.LocalDateTime

// 2026/10/10 - 20:39
// 게시글 상세 조회(게시글 본문)

data class ArticleDetailResponse(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val content: String,
    val authorNickname: String,
    val viewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)