package com.birdiebuddy.birdiebuddyapi.domain.youtube

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 관리자가 영상을 등록할 때 보내는 데이터
data class YoutubeCreateRequest(

    @field:NotBlank(message = "유튜브 링크를 입력해 주세요.")
    @field:Size(
        max = 2048,
        message = "링크는 2048자 이내로 입력해 주세요."
    )
    val url: String = ""
)

// 화면에 보여줄 영상 정보
data class YoutubeResponse(
    val id: Long,
    val videoId: String,
    val title: String,
    val channelName: String
)