package com.birdiebuddy.birdiebuddyapi.domain.article

import java.time.LocalDateTime

// 2026/10/09 - 21:27
// 게시글 목록

data class ArticleListResponse(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val authorId: Long, // 작성자 상세페이지 이동에 사용할 회원 ID
    val authorNickname: String,
    val viewCount: Int,
    val createdAt: LocalDateTime
)