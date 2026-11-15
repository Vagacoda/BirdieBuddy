package com.birdiebuddy.birdiebuddyapi.domain.news.service

import com.birdiebuddy.birdiebuddyapi.domain.news.NaverNewsResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Duration

@Component
class NaverNewsClient(

    // application.yml의 설정값을 주입받음
    @param:Value("\${naver.news.client-id}")
    private val clientId: String,

    @param:Value("\${naver.news.client-secret}")
    private val clientSecret: String
) {

    // 네이버 서버에 연결하거나 응답을 기다리는 시간 제한
    private val requestFactory =
        SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(3))
            setReadTimeout(Duration.ofSeconds(5))
        }

    // 외부 서버에 HTTP 요청을 보내는 도구
    private val restClient = RestClient.builder()
        .baseUrl("https://naverapihub.apigw.ntruss.com")
        .requestFactory(requestFactory)
        .build()

    // 지정한 검색 시작 위치부터 최대 100개의 기사를 최신순으로 조회
    fun searchGolfNews(start: Int = 1): NaverNewsResponse {

        require(start in 1..1000) {
            "검색 시작 위치는 1부터 1000 사이여야 합니다."
        }

        check(clientId.isNotBlank() && clientSecret.isNotBlank()) {
            "네이버 뉴스 API 인증키가 설정되지 않았습니다."
        }

        return restClient.get()
            .uri { builder ->
                builder
                    .path("/search/v1/news")
                    .queryParam("query", "골프")
                    .queryParam("display", 100)
                    .queryParam("start", start)
                    .queryParam("sort", "date")
                    .queryParam("format", "json")
                    .build()
            }
            .header("X-NCP-APIGW-API-KEY-ID", clientId)
            .header("X-NCP-APIGW-API-KEY", clientSecret)
            .retrieve()
            .body(NaverNewsResponse::class.java)
            ?: throw IllegalStateException(
                "네이버 뉴스 API의 응답 본문이 없습니다."
            )
    }
}