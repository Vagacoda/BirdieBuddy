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

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
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