package com.birdiebuddy.birdiebuddyapi.domain.articlevote.service

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleRepository
import com.birdiebuddy.birdiebuddyapi.domain.articlevote.ArticleVoteEntity
import com.birdiebuddy.birdiebuddyapi.domain.articlevote.ArticleVoteRepository
import com.birdiebuddy.birdiebuddyapi.domain.articlevote.ArticleVoteResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
@Transactional
class ArticleVoteService(
    private val articleVoteRepository: ArticleVoteRepository,
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) {
    fun recommendArticle(
        articleId: Long,
        userId: Long
    ): ArticleVoteResponse {
        // 추천할 게시글이 존재하는지 확인
        val article = articleRepository.findByIdAndDeletedAtIsNull(articleId)
            ?: throw IllegalArgumentException(
                "존재하지 않거나 삭제된 게시글입니다."
            )

        // 로그인한 회원이 실제로 존재하는지 확인
        val user = userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("존재하지 않는 회원입니다.")
            }

        // 이 회원이 이 게시글에 남긴 추천 기록 조회
        val existingVote = articleVoteRepository.findByArticle_IdAndUser_Id(
            articleId = articleId,
            userId = userId
        )

        // 이미 추천했다면 저장하지 않고 예외 발생
        if (existingVote != null) {
            throw IllegalArgumentException("이미 추천한 게시글입니다.")
        }

        // 새로운 추천 기록 생성
        val vote = ArticleVoteEntity(
            article = article,
            user = user,
            voteType = "LIKE"
        )

        // DB에 추천 기록 저장
        articleVoteRepository.save(vote)

        // 방금 저장한 추천을 포함한 전체 추천 수 조회
        val voteCount = articleVoteRepository.countByArticle_IdAndVoteType(
            articleId = articleId,
            voteType = "LIKE"
        )

        return ArticleVoteResponse(
            articleId = articleId,
            voteCount = voteCount
        )
    }
}