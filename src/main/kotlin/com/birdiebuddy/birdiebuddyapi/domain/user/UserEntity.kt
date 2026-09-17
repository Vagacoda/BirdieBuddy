package com.birdiebuddy.birdiebuddyapi.domain.user

import com.birdiebuddy.birdiebuddyapi.domain.role.RoleEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

// 2026/09/17 - 20:12 UserEntity 생성
@Entity
@Table
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // 유저 id

    @ManyToOne(fetch = FetchType.LAZY) // 여러회원이 하나의 권한을 공유, user.role을 사용할때 DB사용
    @JoinColumn(name = "role_id",  nullable = false)
    // User테이블의 role_id가 Role테이블과 연결되는 외래키라고 지정, 권한없이 회원은 존재 불가
    var role: RoleEntity? = null,
    // 클래스 안에서 권한 ID대신 RoleEntity 객체를 보관하는 필드, 실제 DB에 저장할때는 반드시 권한 필수

)