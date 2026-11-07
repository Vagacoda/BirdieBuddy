package com.birdiebuddy.birdiebuddyapi.domain.image

// 이미지 업로드 성공 시 프론트엔드에 전달할 응답
data class ImageUploadResponse(
    // 게시글 본문에서 이미지를 표시할 때 사용할 주소
    val url: String
)