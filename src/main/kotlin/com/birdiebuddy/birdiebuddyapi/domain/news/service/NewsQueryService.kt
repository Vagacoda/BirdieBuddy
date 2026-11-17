package com.birdiebuddy.birdiebuddyapi.domain.news.service

import com.birdiebuddy.birdiebuddyapi.domain.news.NewsPageResponse
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsRepository
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NewsQueryService(
    private val newsRepository: NewsRepository
) {

    @Transactional(readOnly = true)
    fun getNews(
        page: Int,
        size: Int,
        keyword: String = ""
    ): NewsPageResponse {

        require(page >= 0) {
            "페이지 번호는 0 이상이어야 합니다."
        }

        require(size in 1..20) {
            "페이지당 기사 수는 1부터 20 사이여야 합니다."
        }

        // 검색어 양 끝의 공백 제거
        val searchKeyword = keyword.trim()

        require(searchKeyword.length <= 100) {
            "검색어는 100자 이내로 입력해 주세요."
        }

        // 발행 시각 최신순, 같은 시각이면 id가 큰 기사부터
        val sort = Sort.by(
            Sort.Order.desc("publishedAt"),
            Sort.Order.desc("id")
        )

        // 요청할 페이지 번호, 페이지 크기, 정렬 조건
        val pageable = PageRequest.of(page, size, sort)

        // 검색어가 없으면 전체 조회, 있으면 제목 검색
        val result = if (searchKeyword.isBlank()) {
            newsRepository.findAll(pageable)
        } else {
            newsRepository.findByTitleContainingIgnoreCase(
                searchKeyword,
                pageable
            )
        }

        // DB 엔터티를 화면 응답용 DTO로 변환
        val news = result.content.map { entity ->
            NewsResponse(
                id = requireNotNull(entity.id),
                title = entity.title,
                summary = entity.summary,
                originalUrl = entity.originalUrl,
                thumbnailUrl = entity.thumbnailUrl,
                publishedAt = entity.publishedAt
            )
        }

        return NewsPageResponse(
            news = news,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }
}