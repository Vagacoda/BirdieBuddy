package com.birdiebuddy.birdiebuddyapi.domain.auth.service

import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
// Spring이 이 클래스를 서비스 객체로 생성하고 관리
class JwtService(
    @Value("\${app.jwt.secret}")
    private val secret: String,
    // 설정 파일의 app.jwt.secret 값을 가져옴
    // JWT에 서명하고, 받은 JWT의 서명을 검증하는 비밀키

    @Value("\${app.jwt.access-token-expiration-ms}")
    private val accessTokenExpirationMs: Long,
    // 설정 파일에서 토큰 유효 시간을 가져옴
    // 단위는 밀리초
    // 예: 3600000밀리초 = 1시간

    private val userRepository: UserRepository
    // JWT를 검사할 때 DB의 회원 정보도 확인하기 위해 사용
    // 회원 상태와 현재 tokenVersion을 조회
) {

    private val signingKey: SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    // secret은 Base64 문자열 형태로 설정되어 있음
    // 1. BASE64.decode(secret): 문자열을 원래 바이트 배열로 변환
    // 2. hmacShaKeyFor(...): JWT 서명에 사용할 키 객체 생성
    // signingKey는 토큰을 만들 때와 검증할 때 모두 사용

    fun createAccessToken(
        userId: Long,
        // 로그인에 성공한 회원의 ID

        tokenVersion: Long
        // 로그인 시점에 DB에 저장된 해당 회원의 토큰 버전
    ): String {
        // 회원 ID와 토큰 버전을 받아 JWT 문자열을 반환

        val now = Date()
        // 토큰 발급 시각을 현재 시간으로 설정

        val expiration = Date(now.time + accessTokenExpirationMs)
        // 현재 시각에 설정된 유효 시간을 더해서 만료 시각 계산
        // 예: 20시에 발급 + 유효 시간 1시간 → 21시에 만료

        return Jwts.builder()
            // 새로운 JWT를 만들기 시작

            .subject(userId.toString())
            // JWT의 subject에 회원 ID 저장
            // subject는 문자열이므로 Long을 String으로 변환
            // 예: 4 → "4"

            .claim("tokenVersion", tokenVersion.toString())
            // JWT에 직접 정의한 정보를 추가
            // claim의 이름은 "tokenVersion"
            // 값은 로그인 당시 회원의 토큰 버전
            // 이 코드에서는 버전도 문자열로 저장
            // 예: tokenVersion이 0이면 "0" 저장

            .issuedAt(now)
            // 토큰을 발급한 시각 기록

            .expiration(expiration)
            // 토큰 만료 시각 기록
            // 만료된 토큰은 이후 검증 과정에서 거부됨

            .signWith(signingKey)
            // 서버의 비밀키로 서명
            // 토큰 내용이 변조되지 않았는지 검증할 수 있게 함
            // 서명은 암호화가 아니므로 비밀번호 같은 비밀정보는 넣으면 안 됨

            .compact()
        // 위에서 설정한 내용을 하나의 JWT 문자열로 완성하여 반환
    }

    fun getUserId(token: String): Long {
        // 받은 JWT를 검증하고 회원 ID를 반환
        // 검증에 실패하면 JwtException 발생
        // 기존 JwtAuthenticationFilter가 이를 받아 401 응답을 보냄

        if (token.isBlank()) {
            // 토큰이 빈 문자열이거나 공백뿐이면 차단
            throw JwtException("토큰이 없습니다.")
        }

        val claims = Jwts.parser()
            // 전달받은 JWT를 읽고 검증할 준비

            .verifyWith(signingKey)
            // 서버의 키로 서명을 검증하도록 설정

            .build()
            // 설정한 JWT 검증 도구 생성

            .parseSignedClaims(token)
            // JWT를 해석하면서 서명과 만료 여부 등을 검증
            // 잘못된 서명, 만료된 토큰 등이면 예외 발생

            .payload
        // 검증된 JWT에서 내용 부분을 가져옴
        // subject, tokenVersion, 발급 시각 등이 들어 있음

        val userId = claims.subject?.toLongOrNull()
            ?: throw JwtException("회원 ID가 올바르지 않습니다.")
        // claims.subject: 토큰에 저장한 회원 ID 문자열
        // ?. : subject가 null이 아닐 때만 변환 실행
        // toLongOrNull(): 숫자로 변환하고, 변환할 수 없으면 null
        // ?: : 결과가 null이면 예외 발생
        // 예: "4" → 4L

        val tokenVersion = claims
            .get("tokenVersion", String::class.java)
            // 토큰에서 "tokenVersion"을 문자열로 꺼냄
            // 발급할 때 문자열로 저장했으므로 문자열로 읽음

            ?.toLongOrNull()
        // 값이 있으면 Long으로 변환
        // 예: "0" → 0L

            ?: throw JwtException("토큰 버전이 없습니다.")
        // 버전이 없거나 숫자로 변환할 수 없다면 차단
        // 이 기능을 추가하기 전에 발급한 토큰도 여기서 차단됨

        val user = userRepository.findById(userId).orElseThrow {
            JwtException("존재하지 않는 회원입니다.")
        }
        // JWT에서 확인한 회원 ID로 DB 조회
        // 토큰이 정상이어도 해당 회원이 DB에 없으면 인증 거부

        if (
            user.deletedAt != null ||
            user.status != "ACTIVE" ||
            user.emailVerifiedAt == null
        ) {
            throw JwtException("이용할 수 없는 계정입니다.")
        }
        // ||는 OR: 조건 중 하나라도 참이면 차단
        // 1. 탈퇴 시각이 기록되어 있거나
        // 2. 정상 계정 상태가 아니거나
        // 3. 이메일 인증을 완료하지 않았다면
        // 해당 토큰으로 인증할 수 없음

        if (user.tokenVersion != tokenVersion) {
            throw JwtException("더 이상 사용할 수 없는 토큰입니다.")
        }
        // DB의 현재 버전과 JWT에 저장한 버전을 비교
        //
        // 예:
        // 로그인 당시 DB 버전 = 0 → JWT에도 0 저장
        // 비밀번호 변경 → DB 버전을 1로 증가
        // 기존 JWT에는 여전히 0이 들어 있음
        // DB의 1과 JWT의 0이 다르므로 기존 JWT는 인증 거부
        //
        // 새 비밀번호로 다시 로그인하면
        // 현재 DB 버전인 1을 담은 JWT가 새로 발급됨

        return userId
        // 모든 검증을 통과한 경우에만 회원 ID 반환
        // JwtAuthenticationFilter가 이 ID를 인증 정보에 저장
    }
}