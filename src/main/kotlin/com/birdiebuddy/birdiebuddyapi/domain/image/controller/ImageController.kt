package com.birdiebuddy.birdiebuddyapi.domain.image.controller

import com.birdiebuddy.birdiebuddyapi.domain.image.ImageUploadResponse
import com.birdiebuddy.birdiebuddyapi.domain.image.service.ImageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/api/images")
class ImageController(
    // Spring이 관리하는 ImageService를 전달받음
    private val imageService: ImageService
) {
    // POST /api/images로 들어오는 파일 업로드 요청 처리
    @PostMapping(
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun upload(
        @RequestParam("image") image: MultipartFile,
        authentication: Authentication
    ): ResponseEntity<ImageUploadResponse> {
        // 기존 JWT 필터가 인증 정보에 넣은 회원 번호
        val userId = authentication.principal as Long

        val result = imageService.upload(
            image = image,
            userId = userId
        )

        return ResponseEntity.ok(result)
    }
}