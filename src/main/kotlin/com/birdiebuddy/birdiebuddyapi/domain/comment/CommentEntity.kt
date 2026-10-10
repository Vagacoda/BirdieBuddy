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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: ArticleEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String = "",

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
) {
    @PreUpdate
    fun updateModifiedTime() {
        updatedAt = LocalDateTime.now()
    }
}

interface CommentRepository : JpaRepository<CommentEntity, Long> {
    fun findAllByArticle_IdAndDeletedAtIsNullOrderByCreatedAtAsc(
        articleId: Long
    ): List<CommentEntity>
}