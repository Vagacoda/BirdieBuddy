package com.birdiebuddy.birdiebuddyapi.domain.article

import java.time.LocalDateTime

// 2026/10/09 - 21:27
//

data class ArticleListResponse(
    val id: Long,
    val type: String,
    val category: String,
    val title: String,
    val authorNickname: String,
    val viewCount: Int,
    val createdAt: LocalDateTime
)