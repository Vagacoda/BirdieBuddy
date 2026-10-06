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
    //  Long은 큰 정수를 담는 타입, 여기서는 밀리초
) {
    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    // secret은 Base64 문자열 형태로 저장
    // Decoders.BASE64.decode(secret)가 문자열을 실제 바이트 값으로 바꿈
    // Keys.hmacShaKeyFor(...)가 그 바이트를 JWT 서명용 SecretKey로 만듦

    fun createAccessToken(userId: Long): String {
        // 회원 ID를 받아 JWT 문자열을 만들어 반환하는 함수
        val now = Date()
        val expiration = Date(now.time + accessTokenExpirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact()
    }
}