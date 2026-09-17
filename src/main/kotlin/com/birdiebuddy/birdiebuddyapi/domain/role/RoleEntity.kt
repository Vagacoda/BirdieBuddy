package com.birdiebuddy.birdiebuddyapi.domain.role

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

// 2026/09/17 -17:43
// 권한 테이블
@Entity
@Table(name = "role")
class RoleEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?= null,

    @Column(nullable = false, unique = true, length = 20)
    var name: String = ""
)