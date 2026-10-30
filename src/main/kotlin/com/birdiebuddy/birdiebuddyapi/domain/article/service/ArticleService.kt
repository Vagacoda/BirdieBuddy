package com.birdiebuddy.birdiebuddyapi.domain.article.service

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleEntity
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleRepository
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
// 2026/10/09 - 21:32
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleListResponse
// 2026/10/10 - 20:53
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleDetailResponse
// 2026/10/24 - 18:43
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleUpdateRequest
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
// 2026/10/27 - 22:56
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticlePageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

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

    // 2026/10/27 - 페이지 검색을 위해 기존 게시글 검색 기능 주석화
    //    fun getArticleList(): List<ArticleListResponse> {
    //        // 한 개가 아니라 여러 개를 반환
    //        val articles = articleRepository
    //            .findAllByDeletedAtIsNullOrderByCreatedAtDesc()
    //
    //        return articles.map { article ->
    //            // map은 목록 안의 데이터를 하나씩 다른 형태로 바꿈
    //            val articleId = article.id
    //                ?: throw IllegalStateException("게시글 ID가 없습니다.")
    //            // 게시글 ID가 null일때 출력
    //
    //            val user = article.user
    //                ?: throw IllegalStateException("게시글 작성자 정보가 없습니다.")
    //            // 유저 없으면 출력
    //
    //            ArticleListResponse(
    //                id = articleId,
    //                type = article.type,
    //                category = article.category,
    //                title = article.title,
    //                authorNickname = user.nickname,
    //                viewCount = article.viewCount,
    //                createdAt = article.createdAt
    //            )
    //        }
    //    }

    fun getArticleList(
        page: Int,
        size: Int,
        keyword: String? = null, // 2026/10/29 - 게시글 검색
        category: String? = null,
        searchType: String = "TITLE"
    ): ArticlePageResponse {
        // 잘못된 페이지 번호와 조회 개수 차단
        // 입력값 검사
        require(page >= 1) {
            "페이지 번호는 1 이상이어야 합니다."
        }
        require(size in 1..100) {
            "한 페이지의 게시글 수는 1~100개여야 합니다."
        }

        // 페이지 번호, 조회 개수, 정렬 조건 설정
        // 조회 조건
        val pageable = PageRequest.of(
            page - 1, // 사용자의 1페이지를 Spring Data의 0페이지로 변환
            size, // 한 페이지에 가져올 글 개수
            Sort.by( //
                Sort.Order.desc("createdAt"), // 작성 시각이 최신인 글부터
                Sort.Order.desc("id") // 작성 시각이 같으면 ID가 큰 글부터
            )
        )

        // 해당 페이지의 게시글 조회
        // DB 조회 후 게시글 목록 변환
        // 게시글 검색 기능 추가로 인한 비활성화
        // val articlePage = articleRepository
        //       .findAllByDeletedAtIsNull(pageable)


        // 2026/10/29 - 게시글 검색 기능
        // 검색어 앞뒤 공백 제거
        // 검색어 앞뒤 공백 제거
    // keyword가 null이면 trim()을 실행하지 않고 null 유지
    // 예: "  golf  " → "golf"
        val searchKeyword = keyword?.trim()

    // 카테고리 앞뒤 공백 제거
    // null 또는 빈 값이면 전체 카테고리를 조회할 예정
        val selectedCategory = category?.trim()

    // 검색 기준의 앞뒤 공백을 제거하고 대문자로 통일
    // 예: " title " → "TITLE"
        val selectedSearchType = searchType.trim().uppercase()

    // 검색 기준은 TITLE(제목), AUTHOR(작성자)만 허용
    // 다른 값이면 IllegalArgumentException 발생
    // 기존 예외 처리 코드에서 400 Bad Request로 응답
        require(selectedSearchType in listOf("TITLE", "AUTHOR")) {
            "검색 기준은 TITLE 또는 AUTHOR여야 합니다."
        }

    // when은 위에서부터 조건을 확인하고
    // 처음 만족하는 분기 하나만 실행
    // 선택된 분기의 조회 결과를 articlePage에 저장
        val articlePage = when {

            // 1. 검색어가 null, 빈 문자열, 공백이면 검색 없이 목록 조회
            searchKeyword.isNullOrBlank() -> {

                // 카테고리도 선택하지 않았다면 전체 카테고리의 글 조회
                if (selectedCategory.isNullOrBlank()) {
                    articleRepository.findAllByDeletedAtIsNull(pageable)
                } else {
                    // 카테고리가 있으면 해당 카테고리의 글만 조회
                    // 예: "FREE" → 자유글 목록
                    articleRepository.findAllByDeletedAtIsNullAndCategory(
                        selectedCategory,
                        pageable
                    )
                }
            }

            // 2. 검색어가 있고, 검색 기준이 제목인 경우
            selectedSearchType == "TITLE" -> {

                // 카테고리를 선택하지 않았다면 전체 글에서 제목 검색
                if (selectedCategory.isNullOrBlank()) {
                    articleRepository
                        .findAllByDeletedAtIsNullAndTitleContainingIgnoreCase(
                            searchKeyword,
                            pageable
                        )
                } else {
                    // 선택한 카테고리 안에서 제목 검색
                    // 예: 자유글 중 제목에 "golf"가 포함된 글
                    articleRepository
                        .findAllByDeletedAtIsNullAndCategoryAndTitleContainingIgnoreCase(
                            selectedCategory,
                            searchKeyword,
                            pageable
                        )
                }
            }

            // 3. 검색어가 있고, 검색 기준이 작성자인 경우
            // 위에서 TITLE과 AUTHOR만 허용했으므로
            // TITLE이 아닌 이 분기는 AUTHOR에 해당
            else -> {

                // 전체 카테고리에서 작성자 닉네임 검색
                // User_Nickname은 article.user.nickname을 의미
                if (selectedCategory.isNullOrBlank()) {
                    articleRepository
                        .findAllByDeletedAtIsNullAndUser_NicknameContainingIgnoreCase(
                            searchKeyword,
                            pageable
                        )
                } else {
                    // 선택한 카테고리 안에서 작성자 닉네임 검색
                    // 예: 자유글 중 작성자 닉네임에 "user"가 포함된 글
                    articleRepository
                        .findAllByDeletedAtIsNullAndCategoryAndUser_NicknameContainingIgnoreCase(
                            selectedCategory,
                            searchKeyword,
                            pageable
                        )
                }
            }
        }

        // 모든 조회 함수는 삭제되지 않은 글만 대상으로 함
        // pageable에는 페이지 번호, 조회 개수, 정렬 조건이 들어 있음
        // 아래의 기존 map 변환과 ArticlePageResponse 반환 코드는 유지
        // Entity 목록을 기존 목록 응답 형태로 변환
        // 해당 페이지의 글과 전체 글 수 등의 정보
        val articles = articlePage.content.map { article ->
            // 예외처리
            val articleId = article.id
                ?: throw IllegalStateException("게시글 ID가 없습니다.")

            val user = article.user
                ?: throw IllegalStateException("게시글 작성자 정보가 없습니다.")

            //  // 게시글 정보를 응답 객체에 담음
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

        // 게시글 목록과 페이지 정보를 함께 반환
        return ArticlePageResponse(
            articles = articles,
            page = page,
            size = size,
            totalElements = articlePage.totalElements,
            totalPages = articlePage.totalPages,
            hasNext = articlePage.hasNext()
        )
    }

    fun getArticleDetail(articleId: Long): ArticleDetailResponse {
        // 게시글 ID를 받아서 글 한 건의 상세 정보를 반환
        val article = articleRepository
            .findByIdAndDeletedAtIsNull(articleId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "존재하지 않거나 삭제된 게시글입니다."
            )
        // findByIdAndDeletedAtIsNull()은 글이 없으면 null을 반환

        article.viewCount += 1
        // 조회수 올라감

        val user = article.user
            ?: throw IllegalStateException("게시글 작성자 정보가 없습니다.")
        // 게시글 작성자 정보를 가져옴

        return ArticleDetailResponse(
            id = article.id
                ?: throw IllegalStateException("게시글 ID가 없습니다."),
            // ID가 없다면 예외처리
            type = article.type,
            category = article.category,
            title = article.title,
            content = article.content,
            // 여기까지가 기본 데이터
            authorNickname = user.nickname,
            // 유저 별명
            viewCount = article.viewCount,
            createdAt = article.createdAt,
            updatedAt = article.updatedAt
            // 각종 시각들
        )
    }
    // 2026/10/24 - 게시글 수정 서비스
    fun updateArticle(
        articleId: Long, //어떤 게시글 수정할지
        userId: Long, // 누가 수정할지
        request: ArticleUpdateRequest // 무엇으로 수정할지
    ) {
        // 수정할 게시글 조회
        val article = articleRepository
            .findByIdAndDeletedAtIsNull(articleId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, // 404 NotFound
                "존재하지 않거나 삭제된 게시글입니다."
            )

        // 게시글 작성자 확인
        val author = article.user
            ?: throw IllegalStateException("게시글 작성자 정보가 없습니다.")

        // 작성자와 수정 요청자가 다르면 차단
        if (author.id != userId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN, // 403 Forbidden
                "본인이 작성한 게시글만 수정할 수 있습니다."
            )
        }

        // 요청받은 값으로 변경
        // ArticleUpdateRequest값을 삽입
        article.category = request.category
        article.title = request.title
        article.content = request.content
    }

    // 게시글 삭제 서비스
    fun deleteArticle(
        articleId: Long,
        userId: Long
    ) {
        // 삭제되지 않은 게시글 조회
        val article = articleRepository
            .findByIdAndDeletedAtIsNull(articleId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND, // 404 NotFound
                "존재하지 않거나 삭제된 게시글입니다."
            )

        // 게시글 작성자 확인
        val author = article.user
            ?: throw IllegalStateException("게시글 작성자 정보가 없습니다.")

        // 작성자 본인만 삭제 가능
        if (author.id != userId) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN, // 401 Forbidden
                "본인이 작성한 게시글만 삭제할 수 있습니다."
            )
        }

        // 삭제 시각을 기록하여 삭제 처리
        article.deletedAt = LocalDateTime.now()
    }
}