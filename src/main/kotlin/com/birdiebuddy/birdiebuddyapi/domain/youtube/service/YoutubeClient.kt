package com.birdiebuddy.birdiebuddyapi.domain.youtube.service

import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeOEmbedResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper
import java.time.Duration

// oEmbed는 “콘텐츠 링크를 보내면, 다른 사이트에 표시하는 데 필요한 정보를 돌려주는 표준 방식
// 유튜브가 제공하는 oEmbed API에 영상 URL을 보내서 정보를 받음, 콘텐츠 정보를 주고받는 요청·응답 형식을 정해둔 API 표준

@Component
class YoutubeClient(
    // JSON을 Kotlin 객체로 변환하는 도구
    private val objectMapper: ObjectMapper
) {

    // 외부 서버 연결과 응답 대기 시간 제한
    private val requestFactory =
        SimpleClientHttpRequestFactory().apply {
            // 연결을 맺을 때 기다리는 시간을 제한
            setConnectTimeout(Duration.ofSeconds(3))
            // 응답 데이터를 읽을 때 기다리는 시간을 제한
            // n초라는 시간 길이를 표현
            setReadTimeout(Duration.ofSeconds(5))
        }

        // 여기서는 요청 도구만 만들고, 아직 유튜브에 요청하지 않음
    private val restClient = RestClient.builder()
        .baseUrl("https://www.youtube.com")
        .requestFactory(requestFactory)
        .build()

    fun getVideoInfo(videoId: String): YoutubeOEmbedResponse {

        require(videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) {
            "올바른 영상 ID가 아닙니다."
        }

        // 입력 링크 대신 검증된 ID로 일정한 형태의 주소를 생성
        val videoUrl = "https://www.youtube.com/watch?v=$videoId"

        // https://www.youtube.com/oembed
        //    ?url=https://www.youtube.com/watch?v=abcdefghijk
        //    &format=json
        val responseBody = restClient.get()
            .uri { builder ->
                builder
                    .path("/oembed")
                    .queryParam("url", videoUrl)
                    .queryParam("format", "json")
                    .build()
            }
            // retrieve()는 응답을 처리하는 단계를 준비
            // body(...)에서 요청을 실행해 응답 본문을 읽음
            // 응답을 문자열로 읽어달라고 자료형을 전달하는 표현
            // responseBody
            //  {
            //   "title": "골프 스윙 연습",
            //   "author_name": "골프 채널"
            // }
            .retrieve()
            .body(String::class.java)
            ?: throw IllegalStateException(
                "유튜브 영상 정보 응답이 없습니다."
            )

        // JSON 문자열을 DTO로 변환
        val info = objectMapper.readValue(
            // 변환할 실제 JSON 문자열
            responseBody,
            // 어떤 클래스의 객체로 만들지
            YoutubeOEmbedResponse::class.java
        )
        // info에는 DTO 객체가 들어감
        // info.title        // "골프 스윙 연습"
        // info.author_name  // "골프 채널"

        check(info.title.isNotBlank() && info.author_name.isNotBlank()) {
            "영상 제목이나 채널 이름을 확인할 수 없습니다."
        }

        return info
    }
}