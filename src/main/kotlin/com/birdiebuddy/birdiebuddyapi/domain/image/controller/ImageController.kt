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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

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
    @GetMapping("/{imageId}")
    fun getImage(
        @PathVariable imageId: Long
    ): ResponseEntity<ByteArray> {
        // DB에서 이미지 정보와 실제 사진 데이터를 조회
        val image = imageService.getImage(imageId)

        return ResponseEntity.ok()
            // 브라우저에 PNG인지 JPEG인지 등을 알려줌
            .contentType(MediaType.parseMediaType(image.contentType))
            // 전송할 데이터 크기
            .contentLength(image.data.size.toLong())
            // JSON 대신 실제 사진 바이트를 응답
            .body(image.data)
    }
}