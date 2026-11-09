package com.birdiebuddy.birdiebuddyapi.domain.role.service

import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class AdminUserService(
    private val userRepository: UserRepository
) {
    // 일반 회원을 영구 차단
    fun blockUser(
        adminId: Long,
        targetUserId: Long
    ) {
        val user = findBlockableMember(
            adminId = adminId,
            targetUserId = targetUserId
        )

        if (user.status == "BLOCKED") {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "이미 차단된 회원입니다."
            )
        }

        user.status = "BLOCKED"
        user.blockedUntil = null

        // 이미 발급된 JWT도 즉시 무효화
        user.tokenVersion += 1
    }

    // 차단된 일반 회원을 다시 활성화
    fun unblockUser(
        adminId: Long,
        targetUserId: Long
    ) {
        val user = findBlockableMember(
            adminId = adminId,
            targetUserId = targetUserId
        )

        if (user.status != "BLOCKED") {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "차단 상태인 회원만 해제할 수 있습니다."
            )
        }

        user.status = "ACTIVE"
        user.blockedUntil = null

        // 해제 후에도 새 로그인으로 시작하게 함
        user.tokenVersion += 1
    }

    // 차단·해제 공통 대상 검사
    private fun findBlockableMember(
        adminId: Long,
        targetUserId: Long
    ): UserEntity {
        if (adminId == targetUserId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "자기 자신은 차단하거나 해제할 수 없습니다."
            )
        }

        val user = userRepository.findById(targetUserId)
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "존재하지 않는 회원입니다."
                )
            }

        if (user.deletedAt != null) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "탈퇴한 회원입니다."
            )
        }

        val roleName = user.role?.name
            ?: throw IllegalStateException("회원 권한 정보가 없습니다.")

        // 관리자끼리 차단해 관리자 기능 전체를 잃는 상황 방지
        if (roleName == "ADMIN") {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "관리자 계정은 차단하거나 해제할 수 없습니다."
            )
        }

        return user
    }
}