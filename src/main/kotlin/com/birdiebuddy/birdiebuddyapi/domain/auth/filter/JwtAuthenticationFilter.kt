package com.birdiebuddy.birdiebuddyapi.domain.auth.filter

import com.birdiebuddy.birdiebuddyapi.domain.auth.service.JwtService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

// 2026/10/06 - 22:00

@Component
// 이 클래스는 Spring이 직접 만들어서 관리
// 생성할 때 JwtService 객체도 자동으로 넣음
class JwtAuthenticationFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {
    // OncePerRequestFilter는 Spring Framework가 제공하는 필터용 클래스
    // OncePerRequestFilter 기능을 상속받음

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
        // 요청 정보, 응답 정보, 다음 필터로 가는 통로를 받음
    ) {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        // 요청 헤더의 Authorization 값을 가져옴

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            // Authorization 헤더가 없거나 또는 헤더가 Bearer로 시작하지 않으면
            filterChain.doFilter(request, response)
            return
        }

        val token = authorizationHeader.removePrefix("Bearer ")

        try {
            val userId = jwtService.getUserId(token)

            val authentication = UsernamePasswordAuthenticationToken(
                userId,
                null,
                emptyList<GrantedAuthority>()
            )

            SecurityContextHolder.getContext().authentication = authentication
        } catch (exception: JwtException) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.writer.write("""{"message":"유효하지 않거나 만료된 토큰입니다."}""")
            return
        }

        filterChain.doFilter(request, response)
    }
}