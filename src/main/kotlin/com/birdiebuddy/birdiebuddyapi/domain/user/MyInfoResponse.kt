package com.birdiebuddy.birdiebuddyapi.domain.user

// 로그인한 회원에게 보여줄 정보만 담는 응답 객체
// 비밀번호 해시 등은 응답에 포함하지 않음
data class MyInfoResponse(
    val id: Long,
    val nickname: String,
    val email: String
)