package com.birdiebuddy.birdiebuddyapi.domain.news.controller

import com.birdiebuddy.birdiebuddyapi.domain.news.NewsPageResponse
import com.birdiebuddy.birdiebuddyapi.domain.news.service.NewsQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/news")
class NewsController(
    private val newsQueryService: NewsQueryService
) {

    @GetMapping
    fun getNews(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "") keyword: String
    ): NewsPageResponse {
        return newsQueryService.getNews(page, size, keyword)
    }
}