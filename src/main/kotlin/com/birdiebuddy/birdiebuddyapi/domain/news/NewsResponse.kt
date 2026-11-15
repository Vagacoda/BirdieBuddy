package com.birdiebuddy.birdiebuddyapi.domain.news

import java.time.LocalDateTime

// 홈페이지와 뉴스 목록에 전달할 기사 한 건
data class NewsResponse(
    val id: Long,
    val title: String,
    val summary: String,
    val originalUrl: String,

    // null이면 프론트에서 기본 이미지 표시
    val thumbnailUrl: String?,

    val publishedAt: LocalDateTime
)

// 기사 목록과 페이지 정보를 함께 전달
data class NewsPageResponse(
    val news: List<NewsResponse>,

    // 페이지 번호는 0부터 시작
    val page: Int,

    // 페이지당 기사 수
    val size: Int,

    // 전체 기사 수
    val totalElements: Long,

    // 전체 페이지 수
    val totalPages: Int
)