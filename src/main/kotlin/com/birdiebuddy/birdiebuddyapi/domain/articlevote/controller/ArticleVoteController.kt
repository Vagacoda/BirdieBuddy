package com.birdiebuddy.birdiebuddyapi.domain.articlevote.controller

import com.birdiebuddy.birdiebuddyapi.domain.articlevote.ArticleVoteResponse
import com.birdiebuddy.birdiebuddyapi.domain.articlevote.service.ArticleVoteService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/articles/{articleId}/votes")
class ArticleVoteController(
    private val articleVoteService: ArticleVoteService
) {
    @PostMapping
    fun recommendArticle(
        @PathVariable articleId: Long,
        authentication: Authentication
    ): ResponseEntity<ArticleVoteResponse> {
        // JWT 인증에서 확인한 로그인 회원 ID
        val userId = authentication.principal as Long

        // 추천 처리 후 게시글 번호와 추천 수를 받음
        val result = articleVoteService.recommendArticle(
            articleId = articleId,
            userId = userId
        )

        return ResponseEntity.ok(result)
    }
}