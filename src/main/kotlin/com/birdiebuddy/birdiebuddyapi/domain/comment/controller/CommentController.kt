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
    @PostMapping
    fun createComment(
        @PathVariable articleId: Long,
        authentication: Authentication,
        @Valid @RequestBody request: CommentCreateRequest
    ): ResponseEntity<CommentResponse> {
        val userId = authentication.principal as Long

        val comment = commentService.createComment(
            articleId = articleId,
            userId = userId,
            request = request
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(comment)
    }

    @GetMapping
    fun getCommentList(
        @PathVariable articleId: Long
    ): ResponseEntity<List<CommentResponse>> {
        val comments = commentService.getCommentList(articleId)

        return ResponseEntity.ok(comments)
    }
}