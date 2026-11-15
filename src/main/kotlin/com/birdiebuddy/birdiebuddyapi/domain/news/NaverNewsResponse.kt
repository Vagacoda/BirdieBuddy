package com.birdiebuddy.birdiebuddyapi.domain.news

// 네이버 뉴스 검색 API의 전체 응답
data class NaverNewsResponse(

    // 검색 결과 전체 개수
    val total: Int = 0,

    // 이번 응답의 검색 시작 위치
    val start: Int = 1,

    // 이번 응답에 포함된 결과 개수
    val display: Int = 0,

    // 검색된 기사 목록
    val items: List<NaverNewsItem> = emptyList()
)

// 검색 결과에 포함된 기사 한 건
data class NaverNewsItem(

    // 기사 제목: 검색어 주변에 <b> 태그가 포함될 수 있음
    val title: String = "",

    // 언론사의 기사 원문 주소
    val originallink: String = "",

    // 네이버 뉴스 주소
    // 네이버에 제공되지 않는 기사는 원문 주소가 들어옴
    val link: String = "",

    // 기사 요약
    val description: String = "",

    // API가 제공하는 기사 시각. 이후 서버 시간대로 변환해서 사용
    val pubDate: String = ""
)