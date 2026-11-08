package com.birdiebuddy.birdiebuddyapi.domain.role.controller

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminController {

    // SecurityConfig의 ADMIN 권한 검사를 통과해야 실행됨
    @GetMapping("/check")
    fun check(
        authentication: Authentication
    ): Map<String, Any> {
        return mapOf(
            "message" to "관리자 권한이 확인되었습니다.",
            "userId" to (authentication.principal as Long)
        )
    }
}