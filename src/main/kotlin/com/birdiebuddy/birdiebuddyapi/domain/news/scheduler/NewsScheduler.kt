package com.birdiebuddy.birdiebuddyapi.domain.news.scheduler

import com.birdiebuddy.birdiebuddyapi.domain.news.service.NewsCollectionService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.UnknownContentTypeException

@Component
class NewsScheduler(
    private val newsCollectionService: NewsCollectionService
) {

    private val logger = LoggerFactory.getLogger(NewsScheduler::class.java)
    // logger는 서버 콘솔이나 로그 파일에 메시지를 남기는 도구
    // NewsScheduler::class.java는 어느 클래스에서 나온 기록인지 식별하기 위해 전달

    // 서버 시작 준비가 끝나면 오늘 수집이 필요한지 확인
    @EventListener(ApplicationReadyEvent::class)
    // Spring Boot는 서버 시작 준비가 끝나면 ApplicationReadyEvent라는 이벤트를 발생
    // 이벤트가 발생하면 이 함수를 실행
    fun collectOnStartup() {
        collectNews()
    }

    // 서버 시간대 기준 매일 오전 9시에 실행
    @Scheduled(cron = "0 0 9 * * *")
    // @Scheduled는 지정한 일정에 따라 함수를 호출
    // 두 실행 경로에서 공통으로 사용하는 함수
    private fun collectNews() {
        try {
            val savedCount = newsCollectionService.collectTodayNews()

            logger.info(
                "뉴스 수집 확인 완료: 새로 저장한 기사 {}개",
                savedCount
            )
        } catch (exception: UnknownContentTypeException) {
            // 응답을 DTO로 변환하지 못한 원인 확인
            logger.error(
                "뉴스 응답 변환 실패: status={}, contentType={}, targetType={}",
                exception.statusCode,
                exception.contentType,
                exception.targetType
            )
        } catch (exception: Exception) {
            // 그 외 오류 기록
            logger.error(
                "뉴스 수집 실패: {}",
                exception.javaClass.simpleName
            )
        }
    }
}