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
        // 요청에서 image라는 이름으로 보낸 파일을 받음
        @RequestParam("image") image: MultipartFile
    ): ResponseEntity<ImageUploadResponse> {

        // 파일 검사와 저장은 Service에 맡김
        val result = imageService.upload(image)

        // 저장된 이미지 URL을 200 OK 응답으로 반환
        return ResponseEntity.ok(result)
    }
}