package com.birdiebuddy.birdiebuddyapi.domain.youtube.service

import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeRepository
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClientException
import org.springframework.web.server.ResponseStatusException

@Service
class YoutubeService(
    private val youtubeUrlParser: YoutubeUrlParser,
    private val youtubeClient: YoutubeClient,
    private val youtubeSaveService: YoutubeSaveService,
    private val youtubeRepository: YoutubeRepository
) {

    // 관리자가 입력한 링크로 영상 등록
    fun registerVideo(request: YoutubeCreateRequest): YoutubeResponse {

        // 링크에서 영상 ID 추출
        val videoId = youtubeUrlParser.extractVideoId(request.url)

        // 이미 등록된 영상이면 외부 API를 호출하기 전에 중단
        require(!youtubeRepository.existsByVideoId(videoId)) {
            "이미 등록된 영상입니다."
        }

        // 유튜브에서 제목과 채널 이름 조회
        val info = try {
            youtubeClient.getVideoInfo(videoId)
        } catch (exception: RestClientException) {
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "영상 정보를 가져오지 못했습니다. 링크를 확인해 주세요."
            )
        }

        // 조회한 정보를 DB에 저장
        return youtubeSaveService.saveVideo(
            videoId = videoId,
            title = info.title,
            channelName = info.author_name
        )
    }

    // 홈 화면에 최근 등록한 영상 최대 10개 표시
    @Transactional(readOnly = true)
    fun getVideos(): List<YoutubeResponse> {

        val pageable = PageRequest.of(
            0,
            10,
            Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
            )
        )

        return youtubeRepository.findAll(pageable)
            .content
            .map { video ->
                YoutubeResponse(
                    id = requireNotNull(video.id),
                    videoId = video.videoId,
                    title = video.title,
                    channelName = video.channelName
                )
            }
    }
}