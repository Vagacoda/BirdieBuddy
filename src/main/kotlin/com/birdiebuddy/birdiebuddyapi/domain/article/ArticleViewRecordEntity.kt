package com.birdiebuddy.birdiebuddyapi.domain.article

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

@Entity
@Table(
    name = "article_view_record",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_article_view_visitor",
            columnNames = ["article_id", "visitor_id"]
        )
    ]
)
class ArticleViewRecordEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "article_id", nullable = false)
    var articleId: Long = 0,

    @Column(name = "visitor_id", nullable = false, length = 36)
    var visitorId: String = "",

    // 마지막 접속 시각이 아니라
    // 마지막으로 조회수를 증가시킨 시각
    @Column(name = "last_counted_at", nullable = false)
    var lastCountedAt: LocalDateTime = LocalDateTime.now()
)

interface ArticleViewRecordRepository :
    JpaRepository<ArticleViewRecordEntity, Long> {

    fun findByArticleIdAndVisitorId(
        articleId: Long,
        visitorId: String
    ): ArticleViewRecordEntity?
}