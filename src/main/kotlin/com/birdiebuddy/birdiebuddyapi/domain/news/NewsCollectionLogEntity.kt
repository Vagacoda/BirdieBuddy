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

    // 수집 기준일이 기본키
    // 날짜별로 완료 기록을 하나만 저장
    @Id
    @Column(name = "collection_date", nullable = false)
    var collectionDate: LocalDate = LocalDate.now(),

    // 수집 범위 시작: 전날 오전 9시, 포함
    @Column(name = "period_start", nullable = false)
    var periodStart: LocalDateTime = LocalDate.now()
        .minusDays(1)
        .atTime(9, 0),

    // 수집 범위 끝: 당일 오전 9시, 미포함
    @Column(name = "period_end", nullable = false)
    var periodEnd: LocalDateTime = LocalDate.now()
        .atTime(9, 0),
