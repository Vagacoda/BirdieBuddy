package com.birdiebuddy.birdiebuddyapi.domain.articlevote

data class ArticleVoteResponse(
    val articleId: Long,
    val voteCount: Long,     // 추천 수
    // 2026/11/03
    val dislikeCount: Long,  // 비추천 수
    val myVote: String?      // 내가 누른 종류: LIKE, DISLIKE 또는 null
)