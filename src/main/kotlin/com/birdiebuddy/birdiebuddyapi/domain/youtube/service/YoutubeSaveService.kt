package com.birdiebuddy.birdiebuddyapi.domain.youtube.service

import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeEntity
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeRepository
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class YoutubeSaveService(
    private val youtubeRepository: YoutubeRepository
) {

    @Transactional
    fun saveVideo(
        videoId: String,
        title: String,
        channelName: String
    ): YoutubeResponse {

        // 같은 영상의 중복 등록 방지
        require(!youtubeRepository.existsByVideoId(videoId)) {
            "이미 등록된 영상입니다."
        }

        // DB 열의 길이에 맞는지 확인
        require(title.isNotBlank() && title.length <= 200) {
            "영상 제목이 비어 있거나 200자를 초과했습니다."
        }

        require(channelName.isNotBlank() && channelName.length <= 200) {
            "채널 이름이 비어 있거나 200자를 초과했습니다."
        }

        // DB에 저장할 엔터티 생성
        val video = YoutubeEntity(
            videoId = videoId,
            title = title,
            channelName = channelName
        )

        val savedVideo = youtubeRepository.save(video)

        // 저장 결과를 화면 응답용 DTO로 변환
        return YoutubeResponse(
            id = requireNotNull(savedVideo.id),
            videoId = savedVideo.videoId,
            title = savedVideo.title,
            channelName = savedVideo.channelName
        )
    }
}