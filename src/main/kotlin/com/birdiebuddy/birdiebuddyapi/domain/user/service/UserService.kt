package com.birdiebuddy.birdiebuddyapi.domain.user.service

import com.birdiebuddy.birdiebuddyapi.domain.user.MyInfoResponse
import com.birdiebuddy.birdiebuddyapi.domain.user.NicknameUpdateRequest
import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import com.birdiebuddy.birdiebuddyapi.domain.user.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
// 2026/11/01 - 20:35
import com.birdiebuddy.birdiebuddyapi.domain.user.PasswordUpdateRequest
import org.springframework.security.crypto.password.PasswordEncoder

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // 기존 내 정보 조회
    fun getMyInfo(userId: Long): MyInfoResponse {
        val user = findActiveUser(userId)

        return toMyInfoResponse(user)
    }

    // 클래스의 readOnly 설정을 덮어쓰고
    // 이 함수에서는 DB 변경을 허용
    @Transactional
    fun updateNickname(
        userId: Long,
        request: NicknameUpdateRequest
    ): MyInfoResponse {
        val user = findActiveUser(userId)

        // 앞뒤 공백 제거
        val nickname = request.nickname.trim()

        require(nickname.isNotBlank()) {
            "닉네임은 필수 입력사항입니다."
        }

        require(nickname.length <= 16) {
            "닉네임은 16자 이하로 입력 가능합니다."
        }

        // 기존 닉네임과 같으면 수정 없이 현재 정보 반환
        if (user.nickname == nickname) {
            return toMyInfoResponse(user)
        }

        // 다른 회원과 닉네임이 중복되는지 확인
        require(!userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            "이미 사용 중인 닉네임입니다."
        }

        user.nickname = nickname

        try {
            // 변경 내용을 지금 DB에 반영
            // 두 사람이 동시에 같은 닉네임을 선택한 경우에도
            // DB의 unique 제약으로 중복 저장을 막음
            userRepository.flush()
        } catch (exception: DataIntegrityViolationException) {
            // 예외를 밖으로 전달해 이번 트랜잭션을 취소
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "닉네임 변경 중 충돌이 발생했습니다. 다른 닉네임으로 다시 시도해 주세요.",
                exception
            )
        }

        return toMyInfoResponse(user)
    }

    // 조회와 수정에서 공통으로 사용하는 회원 상태 검사
    private fun findActiveUser(userId: Long): UserEntity {
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "회원 정보를 확인할 수 없습니다."
            )
        }

        if (user.deletedAt != null || user.status == "WITHDRAWN") {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "탈퇴한 계정입니다."
            )
        }

        if (user.status != "ACTIVE") {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "이용할 수 없는 계정입니다."
            )
        }

        return user
    }

    @Transactional
    fun updatePassword(
        userId: Long,
        request: PasswordUpdateRequest
    ) {
        // 기존에 만든 공통 회원 상태 검사 함수
        val user = findActiveUser(userId)

        // 현재 비밀번호가 맞아야 변경 가능
        require(
            passwordEncoder.matches(
                request.currentPassword,
                user.passwordHash
            )
        ) {
            "현재 비밀번호가 일치하지 않습니다."
        }

        // 같은 비밀번호로 변경하는 것은 차단
        require(
            !passwordEncoder.matches(
                request.newPassword,
                user.passwordHash
            )
        ) {
            "새 비밀번호는 현재 비밀번호와 달라야 합니다."
        }

        // 새 비밀번호를 해시로 변환
        // 결과가 null이면 저장하지 않고 예외 발생
        val newPasswordHash = passwordEncoder.encode(request.newPassword)
            ?: throw IllegalStateException("비밀번호 해시 생성에 실패했습니다.")

        // null이 아닌 해시값을 회원 정보에 저장
        user.passwordHash = newPasswordHash

        // 기존 JWT에 담긴 버전과 달라지므로
        // 변경 전에 발급한 토큰은 이후 인증에서 거부됨
        user.tokenVersion += 1

        // 비밀번호와 토큰 버전을 같은 트랜잭션에서 반영
        // 동시 수정 충돌이 있으면 @Version으로 감지
        userRepository.flush()
    }

    // 응답에는 필요한 정보만 담음
    private fun toMyInfoResponse(user: UserEntity): MyInfoResponse {
        return MyInfoResponse(
            id = user.id
                ?: throw IllegalStateException("회원 ID가 없습니다."),
            nickname = user.nickname,
            email = user.email
        )
    }
}

