package com.birdiebuddy.birdiebuddyapi.domain.user.service

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleListResponse
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticlePageResponse
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import com.birdiebuddy.birdiebuddyapi.domain.user.UserProfileResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional(readOnly = true)
class UserProfileService(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository
) {

    // 공개 프로필 조회
    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = findPublicUser(userId)

        return UserProfileResponse(
            id = user.id
                ?: throw IllegalStateException("회원 ID가 없습니다."),
            nickname = user.nickname
        )
    }

    // 해당 회원의 작성글을 페이지 단위로 조회
    fun getUserArticles(
        userId: Long,
        page: Int,
        size: Int
    ): ArticlePageResponse {
        require(page >= 1) {
            "페이지 번호는 1 이상이어야 합니다."
        }

        require(size in 1..100) {
            "한 페이지의 게시글 수는 1~100개여야 합니다."
        }

        // 글을 조회하기 전에 공개 가능한 회원인지 확인
        val user = findPublicUser(userId)

        val authorId = user.id
            ?: throw IllegalStateException("회원 ID가 없습니다.")

        val pageable = PageRequest.of(
            page - 1,
            size,
            Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
            )
        )

        // 닉네임이 비슷한 다른 회원의 글이 섞이지 않도록
        // 정확한 회원 ID로 조회
        val articlePage = articleRepository
            .findAllByDeletedAtIsNullAndUser_Id(
                authorId,
                pageable
            )

        val articles = articlePage.content.map { article ->
            ArticleListResponse(
                id = article.id
                    ?: throw IllegalStateException("게시글 ID가 없습니다."),
                type = article.type,
                category = article.category,
                title = article.title,
                authorId = authorId,
                authorNickname = user.nickname,
                viewCount = article.viewCount,
                createdAt = article.createdAt
            )
        }

        return ArticlePageResponse(
            articles = articles,
            page = page,
            size = size,
            totalElements = articlePage.totalElements,
            totalPages = articlePage.totalPages,
            hasNext = articlePage.hasNext()
        )
    }

    // 두 조회 함수에서 공통으로 사용하는 회원 확인
    private fun findPublicUser(userId: Long): UserEntity {
        require(userId >= 1) {
            "회원 ID는 1 이상이어야 합니다."
        }

        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "조회할 수 없는 회원입니다."
            )
        }

        // 이번 정책: 탈퇴·정지 등 정상 상태가 아닌 회원은
        // 공개 프로필과 프로필의 작성글 목록을 제공하지 않음
        if (user.deletedAt != null || user.status != "ACTIVE") {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "조회할 수 없는 회원입니다."
            )
        }

        return user
    }
}