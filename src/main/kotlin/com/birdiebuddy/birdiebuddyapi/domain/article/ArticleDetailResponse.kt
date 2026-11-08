package com.birdiebuddy.birdiebuddyapi.domain.article

import java.time.LocalDateTime

data class ArticleDetailResponse(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val content: String,
    val contentFormat: String,

    // 로그인한 회원 ID와 비교해서 본인 글인지 판단
    val authorId: Long,
    val authorNickname: String,

    val viewCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)