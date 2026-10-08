package com.birdiebuddy.birdiebuddyapi.domain.article

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 2026/10/08 - 21:29

// 클라이언트가 보낸 JSON 데이터를 담는 요청 전용 상자

data class ArticleCreateRequest(
    @field:NotBlank(message = "카테고리를 입력해주세요.")
    // "" 또는 "   " 같은 값이면 요청을 거절
    @field:Size(max = 20, message = "카테고리는 20자 이하여야 합니다.")
    // category VARCHAR(20)보다 긴 값이 들어가지 못하게 막음
    val category: String,
    // 카테고리값 받음

    @field:NotBlank(message = "제목을 입력해주세요.")
    @field:Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    val title: String,
    // 제목을 받는 부분

    @field:NotBlank(message = "내용을 입력해주세요.")
    val content: String
    // 게시글 본문 받음
)

// JSON 예시
// {
//   "category": "FREE",
//   "title": "첫 게시글입니다",
//   "content": "BirdieBuddy 게시판 테스트 글입니다."
// }