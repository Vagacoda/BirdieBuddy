package com.birdiebuddy.birdiebuddyapi.domain.articlevote.controller

import com.birdiebuddy.birdiebuddyapi.domain.articlevote.ArticleVoteResponse
import com.birdiebuddy.birdiebuddyapi.domain.articlevote.service.ArticleVoteService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class ArticleVoteRequest(
    val voteType: String
)

@RestController
@RequestMapping("/api/articles/{articleId}/votes")
class ArticleVoteController(
    private val articleVoteService: ArticleVoteService
) {
    // 로그인하지 않아도 추천·비추천 수를 볼 수 있음
    @GetMapping
    fun getVoteState(
        @PathVariable articleId: Long,
        @AuthenticationPrincipal userId: Long?
    ): ResponseEntity<ArticleVoteResponse> {
        return ResponseEntity.ok(
            articleVoteService.getVoteState(
                articleId = articleId,
                userId = userId
            )
        )
    }

    // 투표 변경은 로그인 필요
    @PostMapping
    fun voteArticle(
        @PathVariable articleId: Long,
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: ArticleVoteRequest
    ): ResponseEntity<ArticleVoteResponse> {
        return ResponseEntity.ok(
            articleVoteService.voteArticle(
                articleId = articleId,
                userId = userId,
                voteType = request.voteType
            )
        )
    }
}