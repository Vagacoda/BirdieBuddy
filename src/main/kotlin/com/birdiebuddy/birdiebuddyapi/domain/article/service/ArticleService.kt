package com.birdiebuddy.birdiebuddyapi.domain.article.service

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleEntity
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

// 2026/10/08 - 21:44
// ArticleService

@Service
@Transactional
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) {
    fun createArticle(
        userId: Long,
        request: ArticleCreateRequest
    ): Long {
        val user = userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("존재하지 않는 회원입니다.")
            }

        val article = ArticleEntity(
            user = user,
            type = "NORMAL",
            category = request.category,
            title = request.title,
            content = request.content
        )

        val savedArticle = articleRepository.save(article)

        return savedArticle.id!!
    }
}