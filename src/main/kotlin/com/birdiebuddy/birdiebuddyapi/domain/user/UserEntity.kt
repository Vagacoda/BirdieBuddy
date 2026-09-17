package com.birdiebuddy.birdiebuddyapi.domain.user

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

// 2026/09/17 - 20:12 UserEntity 생성
@Entity
@Table
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

)