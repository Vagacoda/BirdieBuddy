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
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubePageResponse

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

    @Transactional(readOnly = true)
    // 홈은 size=5, 전체 목록은 size=20으로 조회
    fun getVideos(
        page: Int,
        size: Int
    ): YoutubePageResponse {

        require(page >= 0) {
            "페이지 번호는 0 이상이어야 합니다."
        }

        require(size in 1..20) {
            "페이지당 영상 수는 1부터 20 사이여야 합니다."
        }

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
            )
        )

        val result = youtubeRepository.findAll(pageable)

        val videos = result.content.map { video ->
            YoutubeResponse(
                id = requireNotNull(video.id),
                videoId = video.videoId,
                title = video.title,
                channelName = video.channelName
            )
        }

        return YoutubePageResponse(
            videos = videos,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages
        )
    }
}