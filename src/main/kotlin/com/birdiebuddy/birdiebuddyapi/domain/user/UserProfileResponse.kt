package com.birdiebuddy.birdiebuddyapi.domain.user

// 다른 이용자에게 보여줄 공개 정보만 포함
data class UserProfileResponse(
    val id: Long,
    val nickname: String
)