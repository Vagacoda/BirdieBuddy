package com.birdiebuddy.birdiebuddyapi.domain.article

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

// 2026/10/08 - 20:52

@Entity
@Table(name = "`Article`")
class ArticleEntity(
    // 게시글 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 게시글 작성자 연결
    @ManyToOne(fetch = FetchType.LAZY)
    // 한 회원이 다수의 게시글을 작성할 수 있으므로 ManyToOne
    // 회원정보까지 가져오지 않고 article.user를 사용할 때 조회하는 방식
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null,

    @Column(nullable = false, length = 20)
    var type: String = "",

    @Column(nullable = false, length = 20)
    var category: String = "",

    @Column(nullable = false, length = 200)
    var title: String = "",

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    var content: String = "",

    @Column(name = "view_count", nullable = false)
    var viewCount: Int = 0,

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

interface ArticleRepository : JpaRepository<ArticleEntity, Long>