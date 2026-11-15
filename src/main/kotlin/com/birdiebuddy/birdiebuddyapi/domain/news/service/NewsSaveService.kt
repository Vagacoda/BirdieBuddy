package com.birdiebuddy.birdiebuddyapi.domain.news.service

import com.birdiebuddy.birdiebuddyapi.domain.news.NewsCollectionLogEntity
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsCollectionLogRepository
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsEntity
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class NewsSaveService(
    private val newsRepository: NewsRepository,
    private val newsCollectionLogRepository: NewsCollectionLogRepository
) {

    // 기사와 수집 완료 기록을 하나의 트랜잭션으로 저장
    // 중간에 실패하면 이번 트랜잭션의 변경사항을 함께 취소
    @Transactional
    fun saveCollectedNews(
        collectionDate: LocalDate,
        news: List<NewsEntity>
    ): Int {

        // 이미 완료한 날짜라면 저장하지 않음
        if (newsCollectionLogRepository.existsById(collectionDate)) {
            return 0
        }

        val newNews = news
            // 이번에 받은 목록 안에서 같은 기사 제거
            .distinctBy { it.urlHash }

            // 기존 DB에 이미 저장된 기사 제외
            .filterNot {
                newsRepository.existsByUrlHash(it.urlHash)
            }

        // 새 기사들을 저장
        newsRepository.saveAll(newNews)

        // 수집 기준일의 오전 9시
        val periodEnd = collectionDate.atTime(9, 0)

        // 수집 완료 기록 생성
        val log = NewsCollectionLogEntity(
            collectionDate = collectionDate,
            periodStart = periodEnd.minusDays(1),
            periodEnd = periodEnd,
            savedCount = newNews.size,
            completedAt = LocalDateTime.now()
        )

        newsCollectionLogRepository.save(log)

        // 실제 새로 저장한 기사 수 반환
        return newNews.size
    }
}