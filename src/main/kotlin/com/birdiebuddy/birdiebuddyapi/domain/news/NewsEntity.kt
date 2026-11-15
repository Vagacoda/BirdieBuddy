package com.birdiebuddy.birdiebuddyapi.domain.news

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

@Entity
@Table(name = "`News`")
class NewsEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 기사 제목
    @Column(nullable = false, length = 500)
    var title: String = "",

    // 네이버 검색 API가 제공하는 기사 요약
    @Column(nullable = false, columnDefinition = "TEXT")
    var summary: String = "",

    // 제목이나 썸네일을 클릭했을 때 이동할 원문 주소
    @Column(name = "original_url", nullable = false, length = 2048)
    var originalUrl: String = "",

    // 원문 주소를 SHA-256으로 변환한 값
    // DB의 UNIQUE 인덱스로 동일 기사 중복 저장 방지
    @Column(
        name = "url_hash",
        nullable = false,
        length = 64,
        columnDefinition = "CHAR(64)"
    )
    var urlHash: String = "",

    // 대표 이미지가 없으면 null로 저장
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    var thumbnailUrl: String? = null,

    // API에서 받은 기사 시각을 서버 시간대로 변환해 저장
    @Column(name = "published_at", nullable = false)
    var publishedAt: LocalDateTime = LocalDateTime.now(),

    // 우리 서버가 기사를 저장한 시각
    @Column(name = "fetched_at", nullable = false, updatable = false)
    var fetchedAt: LocalDateTime = LocalDateTime.now()
)

interface NewsRepository : JpaRepository<NewsEntity, Long> {

    // 같은 원문 주소의 기사가 이미 저장돼 있는지 확인
    fun existsByUrlHash(urlHash: String): Boolean
}