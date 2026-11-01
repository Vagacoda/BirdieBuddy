package com.birdiebuddy.birdiebuddyapi

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
// 2026/11/01 - 20:40
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus


// 2026/09/25 - 19:28 전역 예외 처리 추가
@RestControllerAdvice // 모든 Controller에서 발생한 오류를 JSON응답으로 처리
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(exception: IllegalArgumentException):
            // 발생한 예외를 exception으로 받고 HTTP응답
            ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body( // badRequest()는 HTTP 상태 코드 400 Bad Request를 의미
            mapOf(
                "message" to (exception.message ?: "잘못된 요청입니다.")
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    // @Valid 검증 오류 처리
    // SignupRequest, LoginRequest 등에 붙인 @NotBlank, @Email, @Pattern 검증이 실패시 발생
    fun handleValidationException(exception: MethodArgumentNotValidException):
            ResponseEntity<Map<String, String>> {
        val message = exception.bindingResult.fieldErrors
            .firstOrNull()
            ?.defaultMessage
            ?: "입력값이 올바르지 않습니다."
            // 검증에 실패한 항목들 중 첫 번째 오류 메시지를 가져옴
        return ResponseEntity.badRequest().body(
            mapOf("message" to message)
        )
    }

    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleConcurrentUpdate(
        exception: OptimisticLockingFailureException
    ): ResponseEntity<Map<String, String>> {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            mapOf(
                "message" to
                        "다른 요청에서 회원 정보가 변경되었습니다. 다시 로그인한 후 시도해 주세요."
            )
        )
    }
}
