package com.birdiebuddy.birdiebuddyapi.domain.articlevote

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
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

// 2026/10/10 - 20:46
// 게시글 추천 로직

@Entity
@Table(name = "`ArticleVote`")
class ArticleVoteEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // 추천 기록 자체의 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    var article: ArticleEntity? = null,
    // 어느 게시글을 추천하는지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,
    // 누가 추천했는지

    @Column(name = "vote_type", nullable = false, length = 10)
    var voteType: String = "LIKE",
    // 추천 종류, 일단은 추천만 가능(비추천은 DISLIKE로 지정 예정)

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
    // 추천 시각
)

interface ArticleVoteRepository : JpaRepository<ArticleVoteEntity, Long> {
    fun findByArticle_IdAndUser_Id(
        articleId: Long,
        userId: Long
    ): ArticleVoteEntity?
    // 이 게시글에 이 회원이 이미 추천한 기록이 있는지 확인

    fun countByArticle_IdAndVoteType(
        articleId: Long,
        voteType: String
    ): Long
    // 이 게시글의 추천이 몇 개인지 확인
}