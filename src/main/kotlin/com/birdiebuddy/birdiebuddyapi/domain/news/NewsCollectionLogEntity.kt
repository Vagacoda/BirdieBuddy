package com.birdiebuddy.birdiebuddyapi.domain.news

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "`NewsCollectionLog`")
class NewsCollectionLogEntity(

    // 어느 날짜의 오전 9시 수집분인지 구분
    // 날짜 자체가 기본키이므로 AUTO_INCREMENT는 사용하지 않음
    @Id
    @Column(name = "collection_date", nullable = false)
    var collectionDate: LocalDate = LocalDate.now(),

    // 수집 시작 시각: 전날 오전 9시, 이 시각 포함
    @Column(name = "period_start", nullable = false)
    var periodStart: LocalDateTime = LocalDate.now()
        .minusDays(1)
        .atTime(9, 0),

    // 수집 종료 시각: 당일 오전 9시, 이 시각 제외
    @Column(name = "period_end", nullable = false)
    var periodEnd: LocalDateTime = LocalDate.now()
        .atTime(9, 0),

    // 중복을 제외하고 실제 새로 저장한 기사 수
    @Column(name = "saved_count", nullable = false)
    var savedCount: Int = 0,

    // 실제 수집 완료 시각
    @Column(name = "completed_at", nullable = false, updatable = false)
    var completedAt: LocalDateTime = LocalDateTime.now()
)

interface NewsCollectionLogRepository :
    JpaRepository<NewsCollectionLogEntity, LocalDate>