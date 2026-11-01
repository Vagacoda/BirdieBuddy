package com.birdiebuddy.birdiebuddyapi.domain.user.service

import com.birdiebuddy.birdiebuddyapi.domain.user.MyInfoResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    fun getMyInfo(userId: Long): MyInfoResponse {
        // JWT에서 확인한 회원 ID로 DB 조회
        // findById는 JpaRepository가 기본으로 제공하는 함수
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "회원 정보를 확인할 수 없습니다."
            )
        }

        // 토큰이 남아 있어도 탈퇴한 회원의 정보는 반환하지 않음
        if (user.deletedAt != null || user.status == "WITHDRAWN") {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "탈퇴한 계정입니다."
            )
        }

        // 정지 등 정상 상태가 아닌 계정은 접근 차단
        if (user.status != "ACTIVE") {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "이용할 수 없는 계정입니다."
            )
        }

        // 엔터티 전체가 아닌 필요한 정보만 응답
        return MyInfoResponse(
            id = user.id
                ?: throw IllegalStateException("회원 ID가 없습니다."),
            nickname = user.nickname,
            email = user.email
        )
    }
}