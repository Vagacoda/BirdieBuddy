package com.birdiebuddy.birdiebuddyapi.domain.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

// 2026/10/06 JWT Token Service 추가

@Service
class JwtService(
    @Value("\${app.jwt.secret}")
    private val secret: String,
    // application.yml의 값을 읽어옴
    // app:
    //  jwt:
    //    secret: '...'

    @Value("\${app.jwt.access-token-expiration-ms}")
    private val accessTokenExpirationMs: Long
    // 만료 시간 값을 읽어옴
    // app:
    //  jwt:
    //    access-token-expiration-ms: 3600000
    // Long은 큰 정수를 담는 타입, 여기서는 밀리초
    // Why? Java의 Date와 now.time이 시간을 밀리초로 다루기 때문
) {
    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    // secret은 Base64 문자열 형태로 저장
    // Decoders.BASE64.decode(secret)가 문자열을 실제 바이트 값으로 바꿈
    // Keys.hmacShaKeyFor(...)가 그 바이트를 JWT 서명용 SecretKey로 만듦

    fun createAccessToken(userId: Long): String {
        // 회원 ID를 받아 JWT 문자열을 만들어 반환하는 함수
        val now = Date()
        // 토큰 발급 시각을 현재 시간으로 저장
        val expiration = Date(now.time + accessTokenExpirationMs)
        // 현재 시간에 1시간을 더해 토큰 만료 시각을 만듦

        return Jwts.builder()
            // JWT를 만듦
            .subject(userId.toString())
            // JWT의 subject 칸에 회원 ID를 넣음
            .issuedAt(now)
            // 토큰 발급 시각을 기록
            .expiration(expiration)
            // 토큰 만료 시각을 기록
            .signWith(signingKey)
            // 서버 비밀키로 JWT에 서명
            .compact()
            // 설정한 정보를 실제 JWT 문자열로 완성

    }
    fun getUserId(token: String): Long {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
            .toLong()
    }
}