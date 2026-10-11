package com.birdiebuddy.birdiebuddyapi.domain.comment.controller

import com.birdiebuddy.birdiebuddyapi.domain.comment.CommentCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.comment.CommentResponse
import com.birdiebuddy.birdiebuddyapi.domain.comment.service.CommentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// 2026/10/11 - 19:09
// CommentController

@RestController
@RequestMapping("/api/articles/{articleId}/comments")
class CommentController(
    private val commentService: CommentService
) {
    // 댓글 작성 요청
    @PostMapping
    fun createComment(
        @PathVariable articleId: Long,
        authentication: Authentication,
        // JSON형태로 받음
        // n번 게시글의 ID를 가져옴
        @Valid @RequestBody request: CommentCreateRequest
    ): ResponseEntity<CommentResponse> {
        // CommentCreateRequest에서 설정한 예외처리문의 규칙에 맞는지 검증.
        // JSON의 content를 CommentCreateRequest 객체로 바꿈
        val userId = authentication.principal as Long
        // JWT에서 받음 유저 ID

        val comment = commentService.createComment(
            articleId = articleId,
            userId = userId,
            request = request // content등의 정보가 있음
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }

    // 댓글 조회 요청
    @GetMapping
    fun getCommentList(
        @PathVariable articleId: Long
    ): ResponseEntity<List<CommentResponse>> {
        val comments = commentService.getCommentList(articleId)

        return ResponseEntity.ok(comments)
    }
}