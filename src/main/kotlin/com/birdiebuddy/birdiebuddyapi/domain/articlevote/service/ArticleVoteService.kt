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
    // 게시글의 추천 수, 비추천 수, 내가 누른 투표를 조회
    fun getVoteState(
        articleId: Long,
        userId: Long?
    ): ArticleVoteResponse {
        // 존재하지 않거나 삭제된 게시글이라면 조회할 수 없음
        findArticle(articleId)

        // 비로그인 사용자는 userId가 null
        // 이 경우 추천·비추천 수는 보여 주되 myVote는 null로 반환
        return createResponse(articleId, userId)
    }

    // 추천 또는 비추천 버튼을 눌렀을 때 실행
    fun voteArticle(
        articleId: Long,
        userId: Long,
        voteType: String
    ): ArticleVoteResponse {
        // 요청에서 허용하는 투표 종류는 LIKE와 DISLIKE뿐
        require(voteType == "LIKE" || voteType == "DISLIKE") {
            "투표 종류는 LIKE 또는 DISLIKE여야 합니다."
        }

        // 투표 대상 게시글 조회
        val article = findArticle(articleId)

        // JWT에서 얻은 회원 ID에 해당하는 회원 조회
        val user = userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("존재하지 않는 회원입니다.")
            }

        // 이 회원이 이 게시글에 이미 투표했는지 확인
        // 회원 한 명은 게시글 하나에 투표 기록 하나만 가짐
        val existingVote = articleVoteRepository.findByArticle_IdAndUser_Id(
            articleId = articleId,
            userId = userId
        )

        when {
            existingVote == null -> {
                // 기존 기록이 없으면 처음 누른 것이므로 새 투표 저장
                articleVoteRepository.save(
                    ArticleVoteEntity(
                        article = article,
                        user = user,
                        voteType = voteType
                    )
                )
            }

            existingVote.voteType == voteType -> {
                // 이미 누른 버튼을 다시 누르면 투표 취소
                // 예: LIKE를 누른 상태에서 LIKE를 다시 누름
                articleVoteRepository.delete(existingVote)
            }

            else -> {
                // 반대 버튼을 누르면 기존 기록의 종류만 변경
                // 예: LIKE 상태에서 DISLIKE를 누르면 비추천으로 변경
                existingVote.voteType = voteType
            }
        }

        // 새 투표·취소·변경을 DB에 반영
        // 이후 개수를 조회할 때 변경 결과가 포함되도록 함
        articleVoteRepository.flush()

        // 변경 후의 추천 수, 비추천 수, 내 선택을 반환
        return createResponse(articleId, userId)
    }

    // 실제로 존재하며 삭제되지 않은 게시글인지 확인하는 공통 함수
    private fun findArticle(articleId: Long) =
        articleRepository.findByIdAndDeletedAtIsNull(articleId)
            ?: throw IllegalArgumentException(
                "존재하지 않거나 삭제된 게시글입니다."
            )

    // 현재 투표 상태를 응답 객체로 만드는 공통 함수
    private fun createResponse(
        articleId: Long,
        userId: Long?
    ): ArticleVoteResponse {
        // 로그인한 회원이라면 이 게시글에 남긴 투표 종류 조회
        // 투표한 적이 없거나 비로그인 상태라면 null
        val myVote = userId?.let {
            articleVoteRepository.findByArticle_IdAndUser_Id(
                articleId = articleId,
                userId = it
            )?.voteType
        }

        // 추천과 비추천은 별도로 개수를 셈
        return ArticleVoteResponse(
            articleId = articleId,
            voteCount = articleVoteRepository.countByArticle_IdAndVoteType(
                articleId = articleId,
                voteType = "LIKE"
            ),
            dislikeCount = articleVoteRepository.countByArticle_IdAndVoteType(
                articleId = articleId,
                voteType = "DISLIKE"
            ),
            myVote = myVote
        )
    }
}