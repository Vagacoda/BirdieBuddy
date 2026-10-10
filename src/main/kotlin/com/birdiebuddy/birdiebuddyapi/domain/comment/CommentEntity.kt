package com.birdiebuddy.birdiebuddyapi.domain.comment

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleEntity
import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

// 2026/10/10 - 21:36
// 댓글 엔터티

@Entity
@Table(name = "`Comment`")
class CommentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // 댓글 ID는 각 게시글에서 갱신되는 것이 아닌 잔체 댓글에서 ID가 증가함

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: ArticleEntity? = null,
    // 댓글이 어느 게시글에 달렸는지 연결
    // DB에는 article_id 숫자가 저장
    // Kotlin에서는 comment.article로 게시글 객체를 다룸
    // nullable = false이므로 게시글 없이 댓글만 존재할 수 없음
    // LAZY는 댓글만 조회할 때 게시글 전체 정보를 바로 가져오지 않는 설정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,
    // 댓글 작성자 연결

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "",
    // 댓글 내용

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    // 댓글 작성 시각

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    // 댓글 수정 시각

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
    // 댓글 삭제 시각
) {
    @PreUpdate
    fun updateModifiedTime() {
        updatedAt = LocalDateTime.now()
        // 댓글 수정 시 JPA가 DB UPDATE를 실행하기 직전에 자동 실행, 댓글 내용이 바뀌면 updated_at도 자동으로 현재 시각으로 변경
    }
}

interface CommentRepository : JpaRepository<CommentEntity, Long> {
    fun findAllByArticle_IdAndDeletedAtIsNullOrderByCreatedAtAsc(
        articleId: Long
    ): List<CommentEntity>
    // 특정 게시글의 댓글 목록을 불러오는 함수
}