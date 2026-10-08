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
        userId: Long, // userId: JWT 인증을 통과한 로그인 회원의 ID
        request: ArticleCreateRequest // request: 사용자가 보낸 카테고리·제목·본문
    ): Long {
        val user = userRepository.findById(userId) // User 테이블에서 작성자 ID로 회원을 찾음
            .orElseThrow { // 회원 조회 결과가 없을 때 처리
                IllegalArgumentException("존재하지 않는 회원입니다.")
            }

        val article = ArticleEntity(
            // DB에 저장할 게시글 객체를 만드는 부분
            user = user, // 로그인 회원을 작성자로 연결
            type = "NORMAL", // 기본은 일반글
            category = request.category, // 사용자가 보낸 요청값 사용
            title = request.title, // 사용자가 보낸 요청값 사용
            content = request.content // 작성·수정 시각은 ArticleEntity의 기본값을 사용
        )

        val savedArticle = articleRepository.save(article)

        return savedArticle.id!!
    }
}