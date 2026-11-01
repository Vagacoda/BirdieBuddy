package com.birdiebuddy.birdiebuddyapi.domain.user.controller

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticlePageResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.UserProfileResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.service.UserProfileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserProfileController(
    private val userProfileService: UserProfileService
) {

    // 예: GET /api/users/5
    @GetMapping("/{userId}")
    fun getUserProfile(
        @PathVariable userId: Long
    ): ResponseEntity<UserProfileResponse> {
        val result = userProfileService.getUserProfile(userId)

        return ResponseEntity.ok(result)
    }

    // 예: GET /api/users/5/articles?page=1&size=10
    @GetMapping("/{userId}/articles")
    fun getUserArticles(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ArticlePageResponse> {
        val result = userProfileService.getUserArticles(
            userId = userId,
            page = page,
            size = size
        )

        return ResponseEntity.ok(result)
    }
}