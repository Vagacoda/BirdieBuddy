package com.birdiebuddy.birdiebuddyapi.domain.article

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// 2026/10/24 - 게시글 수정(게시글 수정 요청)

data class ArticleUpdateRequest(

    @field:NotBlank(message = "카테고리를 입력해주세요.")
    @field:Size(max = 20, message = "카테고리는 20자 이하여야 합니다.")
    val category: String,

    @field:NotBlank(message = "제목을 입력해주세요.")
    @field:Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    val title: String,

    @field:NotBlank(message = "내용을 입력해주세요.")
    val content: String

    // 다음과 같은 데이터가 들어감
    // {
    //   "category": "FREE",
    //   "title": "수정한 제목",
    //   "content": "수정한 본문입니다."
    // }
)