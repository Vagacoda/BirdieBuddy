package com.birdiebuddy.birdiebuddyapi.domain.article

// 2026/10/27 - 게시글 목록과 페이지 정보를 담는 응답 클래스

data class ArticlePageResponse(
    val articles: List<ArticleListResponse>, // 현재 페이지에 표시할 게시글 목록	ex : 게시글 10개
    val page: Int, // 현재 페이지 번호 ex : 1
    val size: Int, // 한 페이지당 최대 게시글 수 ex : 10
    val totalElements: Long, // 조회 조건에 맞는 전체 게시글 수 ex : 23
    val totalPages: Int, // 전체 페이지 수 ex : 3
    val hasNext: Boolean // 다음 페이지가 있는지 ex : true
)