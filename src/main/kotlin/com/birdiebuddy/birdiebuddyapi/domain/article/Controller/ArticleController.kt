package com.birdiebuddy.birdiebuddyapi.domain.article.controller

import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleCreateRequest
import com.birdiebuddy.birdiebuddyapi.domain.article.service.ArticleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
// 2026/10/09 - 22:38
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleListResponse
import org.springframework.web.bind.annotation.GetMapping
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleDetailResponse
import org.springframework.web.bind.annotation.PathVariable
// 2026/10/21 - 19:21
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticleUpdateRequest
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
// 2026/10/27 - 23:18
import com.birdiebuddy.birdiebuddyapi.domain.article.ArticlePageResponse
import org.springframework.web.bind.annotation.RequestParam

// 2026/10/08 - 22:23
// ArticleController

@RestController
@RequestMapping("/api/articles")
class ArticleController(
    private val articleService: ArticleService
) {
    // 2026/10/09 - 22:44
    // 게시글 목록 조회
    // 2026/10/29 - 게시글 검색 및 목록 조회로 인하여 비활성화
    // @GetMapping
    // fun getArticleList(
    //     @RequestParam(defaultValue = "1") page: Int,
    //     @RequestParam(defaultValue = "10") size: Int
    // ): ResponseEntity<ArticlePageResponse> {
    //     val result = articleService.getArticleList(
    //         page = page,
    //         size = size
    //     )

    //     return ResponseEntity.ok(result)
    // }

    @PostMapping
    fun createArticle(
        authentication: Authentication,// JWT로 로그인 확인이 끝난 회원 정보를 받는 변수
        @Valid @RequestBody request: ArticleCreateRequest
        // Valid : ArticleCreateRequest에 붙인 입력값 검사 규칙을 실행 (ex: 빈값이면 400 Bad Request로 막음)
        // RequestBody : HTTP 요청 본문의 JSON을 Kotlin 객체로 바꾸라는 요청
    ): ResponseEntity<Map<String, Any>> {
        val userId = authentication.principal as Long
        // JWT 필터가 저장한 로그인 회원 ID를 꺼냄

        val articleId = articleService.createArticle(
            userId = userId,
            request = request
        )
        // Controller가 저장하는 것이 아니라 Service에서 처리하도록 함.
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "id" to articleId,
                "message" to "게시글이 작성되었습니다."
            )
            // JSON형태로 반환
        )
    }

    // 상세조회
    @GetMapping("/{articleId}")
    fun getArticleDetail(
        @PathVariable articleId: Long
        // 게시글 번호를 함수 변수로 받음
        // URL 경로의 값을 가져오라는 뜻
        // GET /api/articles/1
        // → URL의 1을 꺼냄
        // → articleId: Long에 1 저장
    ): ResponseEntity<ArticleDetailResponse> {
        // 상세 게시글 한 건을 HTTP 응답으로 반환
        // ResponseEntity: 상태 코드와 응답 본문을 함께 반환
        // ArticleDetailResponse: 제목, 본문, 작성자, 조회 수, 작성·수정 시각을 담은 상세 정보
        val article = articleService.getArticleDetail(articleId)
        // articleService가 DB조회 하도록 함

        return ResponseEntity.ok(article)
        //200 OK 상태 코드와 함께 JSON으로 반환
    }

    // 2026/10/29 - 게시글 검색
    // 로직 변경으로 인하여 비활성화
//    @GetMapping
//    fun getArticleList(
//        @RequestParam(defaultValue = "1") page: Int,
//        @RequestParam(defaultValue = "10") size: Int,
//        @RequestParam(required = false) keyword: String?
//    ): ResponseEntity<ArticlePageResponse> {
//        val result = articleService.getArticleList(
//            page = page,
//            size = size,
//            keyword = keyword
//        )
//
//        return ResponseEntity.ok(result)
//    }

    @GetMapping
    fun getArticleList(
        // 생략하면 1페이지
        @RequestParam(defaultValue = "1") page: Int,

        // 생략하면 한 페이지에 최대 10개
        @RequestParam(defaultValue = "10") size: Int,

        // 검색창에 입력한 검색어, 생략 가능
        @RequestParam(required = false) keyword: String?,

        // 목록 위에서 선택한 카테고리, 생략하면 전체
        @RequestParam(required = false) category: String?,

        // 제목 또는 작성자 검색, 생략하면 제목 검색
        @RequestParam(defaultValue = "TITLE") searchType: String
    ): ResponseEntity<ArticlePageResponse> {

        // 요청에서 받은 조건들을 Service에 전달
        val result = articleService.getArticleList(
            page = page,
            size = size,
            keyword = keyword,
            category = category,
            searchType = searchType
        )

        // 조회 결과를 200 OK와 함께 반환
        return ResponseEntity.ok(result)
    }

    // 게시글 수정
    @PutMapping("/{articleId}") // Put(기존데이터 수정)요청을 받아서 다음 함수실행
    fun updateArticle(
        @PathVariable articleId: Long,
        authentication: Authentication,
        @Valid @RequestBody request: ArticleUpdateRequest
    ): ResponseEntity<Map<String, String>> {
        // JWT 인증 정보에서 수정 요청자의 회원 ID를 꺼냄
        val userId = authentication.principal as Long

        // 게시글 조회, 작성자 확인, 내용 수정을 Service에 맡김
        articleService.updateArticle(
            articleId = articleId,
            userId = userId,
            request = request
        )

        return ResponseEntity.ok(
            mapOf("message" to "게시글이 수정되었습니다.")
        )
    }

    // 게시글 삭제
    @DeleteMapping("/{articleId}")
    fun deleteArticle(
        @PathVariable articleId: Long,
        authentication: Authentication
    ): ResponseEntity<Map<String, String>> {
        // JWT로 확인한 로그인 회원 ID
        val userId = authentication.principal as Long

        // 작성자 확인과 삭제 처리를 Service에 맡김
        articleService.deleteArticle(
            articleId = articleId,
            userId = userId
        )

        return ResponseEntity.ok(
            mapOf("message" to "게시글이 삭제되었습니다.")
        )
    }
}