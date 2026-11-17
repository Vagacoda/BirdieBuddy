package com.birdiebuddy.birdiebuddyapi.domain.youtube.controller

import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubePageResponse
import com.birdiebuddy.birdiebuddyapi.domain.youtube.YoutubeResponse
import com.birdiebuddy.birdiebuddyapi.domain.youtube.service.YoutubeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class YoutubeController(
    private val youtubeService: YoutubeService
) {

    // 누구나 영상 목록 조회 가능
    @GetMapping("/api/youtube")
    fun getVideos(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): YoutubePageResponse {
        return youtubeService.getVideos(page, size)
    }

    // 관리자만 영상 등록 가능
    @PostMapping("/api/admin/youtube")
    fun registerVideo(
        @Valid @RequestBody request: YoutubeCreateRequest
    ): ResponseEntity<YoutubeResponse> {

        val video = youtubeService.registerVideo(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(video)
    }
}