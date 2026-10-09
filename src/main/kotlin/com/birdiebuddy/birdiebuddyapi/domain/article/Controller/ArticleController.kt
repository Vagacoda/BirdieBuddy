package com.birdiebuddy.birdiebuddyapi.domain.article.controller

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.article.service.ArticleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
// 2026/10/09 - 22:38
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleListResponse
import org.springframework.web.bind.annotation.GetMapping

// 2026/10/08 - 22:23
// ArticleController

@RestController
@RequestMapping("/api/articles")
class ArticleController(
    private val articleService: ArticleService
) {
    // 2025/10/09 - 22:44
    @GetMapping
    fun getArticleList(): ResponseEntity<List<ArticleListResponse>> {
        val articles = articleService.getArticleList()

        return ResponseEntity.ok(articles)
    }

    @PostMapping
    fun createArticle(
        authentication: Authentication,// JWT로 로그인 확인이 끝난 회원 정보를 받는 변수
        @Valid @RequestBody request: ArticleCreateRequest
        // Valid : ArticleCreateRequest에 붙인 입력값 검사 규칙을 실행 (ex: 빈값이면 400 Bad Request로 막음)
        // RequestBody : HTTP 요청 본문의 JSON을 Kotlin 객체로 바꾸라는 요청
    ): ResponseEntity<Map<String, Any>> {
        val userId = authentication.principal as Long
        // JWT 필터가 저장한 로그인 회원 ID를 꺼냄

        val articleId = articleService.createArticle(
            userId = userId,
            request = request
        )
        // Controller가 저장하는 것이 아니라 Service에서 처리하도록 함.
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "id" to articleId,
                "message" to "게시글이 작성되었습니다."
            )
            // JSON형태로 반환
        )
    }
}