package com.birdiebuddy.birdiebuddyapi.domain.role.controller

import com.birdiebuddy.birdiebuddyapi.domain.article.service.ArticleService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.birdiebuddy.birdiebuddyapi.domain.comment.service.CommentService
import com.birdiebuddy.birdiebuddyapi.domain.role.BlockUserRequest
import com.birdiebuddy.birdiebuddyapi.domain.role.service.AdminUserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val articleService: ArticleService,
    private val commentService: CommentService
) {

    @GetMapping("/check")
    fun check(
        authentication: Authentication
    ): Map<String, Any> {
        return mapOf(
            "message" to "관리자 권한이 확인되었습니다.",
            "userId" to (authentication.principal as Long)
        )
    }

    // 예: DELETE /api/admin/articles/10
    @DeleteMapping("/articles/{articleId}")
    fun deleteArticle(
        @PathVariable articleId: Long
    ): Map<String, String> {
        articleService.deleteArticleByAdmin(articleId)

        return mapOf(
            "message" to "관리자 권한으로 게시글을 삭제했습니다."
        )
    }

    @DeleteMapping("/articles/{articleId}/comments/{commentId}")
    fun deleteComment(
        @PathVariable articleId: Long,
        @PathVariable commentId: Long
    ): Map<String, String> {
        commentService.deleteCommentByAdmin(
            articleId = articleId,
            commentId = commentId
        )

        return mapOf(
            "message" to "관리자 권한으로 댓글을 삭제했습니다."
        )
    }
}