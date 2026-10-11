package com.birdiebuddy.birdiebuddyapi.domain.comment.service

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleRepository
import com.birdiebuddy.birdiebuddyapi.domain.comment.CommentCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.comment.CommentEntity
import com.birdiebuddy.birdiebuddyapi.domain.comment.CommentRepository
import com.birdiebuddy.birdiebuddyapi.domain.comment.CommentResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

// 2026/10/11 - 18:39
// 댓글 저장과 댓글 목록 조회

@Service
@Transactional
class CommentService(
    private val commentRepository: CommentRepository,
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) {
    fun createComment(
        articleId: Long,
        // 댓글을 달 게시글 번호
        userId: Long,
        // JWT에서 확인한 로그인 회원 번호
        request: CommentCreateRequest
        // 사용자가 보낸 댓글 내용
    ): CommentResponse {
        val article = articleRepository
            .findByIdAndDeletedAtIsNull(articleId)
            ?: throw IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다.")
        // 댓글이 실제로 존재하는지 확인, 없으면 예외처리

        val user = userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("존재하지 않는 회원입니다.")
            }
        // JWT에서 받은 회원 ID가 실존하는지 확인, 없으면 예외처리

        val comment = CommentEntity(
            article = article,
            // 어떤 게시글에 달린건지
            user = user,
            // 어떤 유저인지
            content = request.content
            // 어떤 댓글인지
        )

        val savedComment = commentRepository.save(comment)
        // 완성한 댓글을 Comment 테이블에 저장

        return toCommentResponse(savedComment)
        // 저장된 댓글 Entity를 사용자에게 반환할 CommentResponse로 바꿈
    }

    fun getCommentList(articleId: Long): List<CommentResponse> {
        articleRepository.findByIdAndDeletedAtIsNull(articleId)
            ?: throw IllegalArgumentException("존재하지 않거나 삭제된 게시글입니다.")
        // 댓글 목록을 보기 전에도 게시글이 존재하는지 확인, 없으면 예외처리

        val comments = commentRepository
            .findAllByArticle_IdAndDeletedAtIsNullOrderByCreatedAtAsc(articleId)
        // 게시글의 삭제되지 않은 댓글들을 작성 시간 순서대로 가져옴

        return comments.map { comment ->
            toCommentResponse(comment)
        }
        // CommentEntity 목록을 CommentResponse 목록으로 바꿈
    }


    // 댓글 Entity를 응답 객체로 바꾸는 공통 함수
    private fun toCommentResponse( // CommentService 내부에서만 사용되기 때문에 private
        comment: CommentEntity
    ): CommentResponse {
        val commentId = comment.id
            ?: throw IllegalStateException("댓글 ID가 없습니다.")
        // 댓글 ID있는지 확인

        val user = comment.user
            ?: throw IllegalStateException("댓글 작성자 정보가 없습니다.")
        // 댓글 작성자가 있는지 확인

        return CommentResponse(
            id = commentId,
            content = comment.content,
            authorNickname = user.nickname,
            createdAt = comment.createdAt,
            updatedAt = comment.updatedAt
        )
    }
}