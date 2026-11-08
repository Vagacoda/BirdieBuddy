package com.birdiebuddy.birdiebuddyapi.domain.auth
import com.birdiebuddy.birdiebuddyapi.domain.auth.AuthenticatedUser
import org.springframework.transaction.annotation.Transactional

data class AuthenticatedUser(
    val userId: Long,
    val roleName: String
)