package com.birdiebuddy.birdiebuddyapi.domain.youtube.service

import org.springframework.stereotype.Component
import java.net.URI

@Component
class YoutubeUrlParser {

    fun extractVideoId(url: String): String {

        // 입력한 문자열을 URL 구조로 해석
        // URI는 주소를 분석해서 각 부분을 꺼낼 수 있게 해주는 Java 클래스
        // uri.scheme   -> "https"
        // uri.host     -> "www.youtube.com"
        // uri.path     -> "/watch"
        // uri.rawQuery -> "v=abcdefghijk&t=30s"
        // scheme://host/path/rawQuery
        val uri = requireNotNull(
            runCatching { URI(url.trim()) }.getOrNull()
        ) {
            "올바른 유튜브 링크를 입력해 주세요."
        }

        require(
            uri.scheme?.lowercase() in setOf("http", "https")
                    && uri.userInfo == null
        ) {
            "올바른 유튜브 링크를 입력해 주세요."
        }

        // host = "www.youtube.com"
        // path = "/watch"
        val host = uri.host?.lowercase()
        val path = uri.path.orEmpty()

        val videoId = when (host) {

            // https://youtu.be/영상ID
            // 짧은 공유 링크 처리
            "youtu.be", "www.youtu.be" -> {
                path.removePrefix("/").removeSuffix("/")
            }

            // 일반 유튜브 링크 처리
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "music.youtube.com" -> {

                // 경로가 /watch인지 확인
                when {
                    // https://www.youtube.com/watch?v=영상ID
                    path == "/watch" -> {
                        uri.rawQuery.orEmpty()
                            // 유튜브 링크에서 ? 뒤는 이름=값 형태의 옵션들이고, 여러 옵션을 &로 구분하기 때문
                            // "v=abcdefghijk"
                            // "t=30s"
                            .split("&")
                            .firstOrNull {
                                it.substringBefore("=") == "v"
                            }
                            ?.substringAfter("=", "")
                            .orEmpty()
                    }

                    // Shorts, 임베드, 라이브 공유 링크
                    path.startsWith("/shorts/")
                            || path.startsWith("/embed/")
                            || path.startsWith("/live/") -> {
                        path.split("/").getOrNull(2).orEmpty()
                    }

                    else -> ""
                }
            }

            else -> throw IllegalArgumentException(
                "유튜브 링크만 등록할 수 있습니다."
            )
        }

        // 유튜브 영상 ID는 11자리의 영문·숫자·밑줄·하이픈
        require(videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) {
            "영상 ID를 확인할 수 없습니다. 영상 공유 링크를 입력해 주세요."
        }

        return videoId
    }
}