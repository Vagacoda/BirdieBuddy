package com.birdiebuddy.birdiebuddyapi.domain.article.service

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleEntity
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
// 2026/10/09 - 21:32
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleListResponse

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
        // 완성한 게시글 객체를 DB Article 테이블에 저장

        return savedArticle.id!! // !!: 절대로 null이 아니다
        // 저장된 게시글 ID를 Controller에반환
    }

    // 2026/10/09 - 21:32
    // 삭제되지 않은 게시글 전체를 최신순으로 조회
    //→ ArticleEntity 목록을 하나씩 꺼냄
    //→ 목록 화면에 필요한 값만 ArticleListResponse로 변환
    //→ ArticleListResponse 목록 반환

    fun getArticleList(): List<ArticleListResponse> {
        // 한 개가 아니라 여러 개를 반환
        val articles = articleRepository
            .findAllByDeletedAtIsNullOrderByCreatedAtDesc()

        return articles.map { article ->
            // map은 목록 안의 데이터를 하나씩 다른 형태로 바꿈
            val articleId = article.id
                ?: throw IllegalStateException("게시글 ID가 없습니다.")
            // 게시글 ID가 null일때 출력

            val user = article.user
                ?: throw IllegalStateException("게시글 작성자 정보가 없습니다.")
            // 유저 없으면 출력

            ArticleListResponse(
                id = articleId,
                type = article.type,
                category = article.category,
                title = article.title,
                authorNickname = user.nickname,
                viewCount = article.viewCount,
                createdAt = article.createdAt
            )
        }
    }
}