package com.birdiebuddy.birdiebuddyapi.domain.news.service

import com.birdiebuddy.birdiebuddyapi.domain.news.NaverNewsItem
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsCollectionLogRepository
import com.birdiebuddy.birdiebuddyapi.domain.news.NewsEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.net.URI
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.HexFormat

@Service
class NewsCollectionService(
    private val naverNewsClient: NaverNewsClient,
    private val newsSaveService: NewsSaveService,
    private val newsCollectionLogRepository: NewsCollectionLogRepository
) {

    // 한 서버 안에서 수집 작업이 동시에 실행되지 않도록 처리
    // 같은 서비스 객체에서 이 함수를 동시에 호출해도 한 번에 하나씩 실행
    @Synchronized
    fun collectTodayNews(): Int {

        // 기준 시각과 서버 시간대를 한 번만 계산
        val zone = ZoneId.systemDefault() // 네이버에 기사 요청
        val now = LocalDateTime.now(zone) // 기사와 완료 기록을 DB에 저장
        val collectionDate = now.toLocalDate() // 오늘 수집 완료 여부 확인

        val periodEnd = collectionDate.atTime(9, 0)
        val periodStart = periodEnd.minusDays(1)

        // 오늘 오전 9시 이전이면 실행하지 않음
        if (now.isBefore(periodEnd)) {
            return 0
        }

        // 오늘 이미 수집을 완료했다면 실행하지 않음
        if (newsCollectionLogRepository.existsById(collectionDate)) {
            return 0
        }

        val collectedNews = mutableListOf<NewsEntity>() // 항목을 추가하거나 제거할 수 있는 목록
        var start = 1

        while (true) {

            // 네이버에서 최신순으로 최대 100개 조회
            val response = naverNewsClient.searchGolfNews(start)
            val items = response.items

            // 더 이상 검색 결과가 없으면 수집 종료
            if (items.isEmpty()) {
                break
            }

            var reachedOlderNews = false

            for (item in items) { // item은 기사

                // API의 시각 문자열을 서버 시간대의 날짜·시간으로 변환
                // 변환에 실패하면 예외를 발생시켜 완료로 기록하지 않음
                val publishedAt = ZonedDateTime.parse( // publishedAt는 변환한 기사 시각
                    item.pubDate,
                    // "pubDate": "Sun, 15 Nov 2026 08:30:00 +0900"
                    DateTimeFormatter.RFC_1123_DATE_TIME
                    // Sun, 15 Nov 2026 08:30:00 +0900
                )
                    .withZoneSameInstant(zone) // 서버 시간대로 맞춤
                    .toLocalDateTime() // 날짜와 시각만 꺼냄

                // 오늘 오전 9시 이후 기사는 다음 수집분이므로 제외
                if (!publishedAt.isBefore(periodEnd)) {
                    continue
                }

                // 어제 오전 9시보다 오래된 기사에 도달했는지 확인
                if (publishedAt.isBefore(periodStart)) {
                    reachedOlderNews = true
                    continue
                }

                collectedNews.add(
                    toEntity(item, publishedAt, now)
                )
            }

            // 최신순 결과에서 수집 시작 시각보다 오래된 기사까지 확인
            if (reachedOlderNews) {
                break
            }

            // 검색 결과의 마지막까지 확인했다면 종료
            val nextStart = start + items.size

            if (nextStart > response.total) {
                break
            }

            // 조회 한도에 도달하면 지금까지 모은 기사로 처리
            if (nextStart > 901) {
                break
            }

            start = nextStart
        }

        // 조회 가능한 범위에서 모은 기사를 저장하고 오늘 처리 완료 기록
        return newsSaveService.saveCollectedNews(
            collectionDate = collectionDate,
            news = collectedNews
        )
    }

    // 네이버 기사 DTO를 DB 저장용 엔터티로 변환
    private fun toEntity(
        item: NaverNewsItem, // 네이버에서 받은 기사 한 건
        publishedAt: LocalDateTime, // 변환한 기사 시각
        fetchedAt: LocalDateTime // 이번 수집을 시작한 서버 시각
    ): NewsEntity {

        val originalUrl = item.originallink.trim()
        val uri = URI(originalUrl)

        // 원문 주소가 정상적인 HTTP 또는 HTTPS 주소인지 확인
        check(
            uri.scheme?.lowercase() in setOf("http", "https")
                    && !uri.host.isNullOrBlank()
                    && uri.userInfo == null
                    && originalUrl.length <= 2048
        ) {
            "기사 원문 주소가 올바르지 않습니다."
        }

        val title = cleanText(item.title)

        check(title.isNotBlank() && title.length <= 500) {
            "기사 제목이 비어 있거나 저장 가능한 길이를 초과했습니다."
        }

        // 같은 원문 URL은 항상 같은 SHA-256 해시값을 가짐
        val hashBytes = MessageDigest.getInstance("SHA-256")
            .digest(originalUrl.toByteArray(Charsets.UTF_8))

        val urlHash = HexFormat.of().formatHex(hashBytes)

        return NewsEntity(
            title = title,
            summary = cleanText(item.description),
            originalUrl = originalUrl,
            urlHash = urlHash,

            // 우선 기본 이미지를 사용하고 실제 썸네일 수집은 이후 추가
            thumbnailUrl = null,

            publishedAt = publishedAt,
            fetchedAt = fetchedAt
        )
    }

    // 검색어 강조 태그 제거 후 HTML 문자 표현을 일반 문자로 변환
    private fun cleanText(value: String): String {
        val withoutBoldTags = value.replace(
            Regex("</?b>", RegexOption.IGNORE_CASE),
            ""
        )

        return HtmlUtils.htmlUnescape(withoutBoldTags).trim()
    }
}