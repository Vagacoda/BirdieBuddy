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

// 2026/10/08 - 22:23
// ArticleController

@RestController
@RequestMapping("/api/articles")
class ArticleController(
    private val articleService: ArticleService
) {
    @PostMapping
    fun createArticle(
        authentication: Authentication,
        @Valid @RequestBody request: ArticleCreateRequest
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