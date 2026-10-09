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
    // user_id 열을 외래 키로 사용, 비어있을 수 없으므로 nullable은 false
    var user: UserEntity? = null,


    // 게시글 타입(일반글, 공지글 구분용)
    @Column(nullable = false, length = 20)
    var type: String = "NORMAL",

    // 게시글 카테고리 (ex: 자유글, 질문글 등)
    @Column(nullable = false, length = 20)
    var category: String = "",


    // 게시글 제목
    @Column(nullable = false, length = 200)
    var title: String = "",

    // 게시글 내용
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    var content: String = "",

    // 조회수
    @Column(name = "view_count", nullable = false)
    var viewCount: Int = 0,

    // 게시글 작성시각
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // 게시글 수정 시각
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    // 게시글 삭제 시각
    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
) {
    @PreUpdate
    fun updateModifiedTime() {
        updatedAt = LocalDateTime.now()
        // JPA가 기존 게시글을 수정해서 DB의 UPDATE SQL을 실행하기 직전에 자동으로 호출,
        // 제목이나 본문을 수정하면 updated_at이 자동으로 현재 시각으로 바뀜
    }
}

interface ArticleRepository : JpaRepository<ArticleEntity, Long>{
    // 2026/10/09 - 21:24
    // 삭제되지 않은 글만, 최신 글부터
    fun findAllByDeletedAtIsNullOrderByCreatedAtDesc(): List<ArticleEntity>
}
// 게시글 테이블에 접근하는 도구