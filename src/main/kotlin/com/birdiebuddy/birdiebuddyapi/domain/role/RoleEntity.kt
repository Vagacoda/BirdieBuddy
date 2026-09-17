package com.birdiebuddy.birdiebuddyapi.domain.role

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

// 2026/09/17 - 20:07 RoleEntity 생성
@Entity
@Table(name = "Role")
class RoleEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true, length = 20)
    var name: String = ""
)

interface RoleRepository : JpaRepository<RoleEntity, Long>