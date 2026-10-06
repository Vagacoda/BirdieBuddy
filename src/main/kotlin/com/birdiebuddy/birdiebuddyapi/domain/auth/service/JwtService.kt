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

    @Value("\${app.jwt.access-token-expiration-ms}")
    private val accessTokenExpirationMs: Long
) {
    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun createAccessToken(userId: Long): String {
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