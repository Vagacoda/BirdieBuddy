package com.birdiebuddy.birdiebuddyapi.domain.user

import com.birdiebuddy.birdiebuddyapi.domain.role.RoleEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import javax.accessibility.AccessibleState.ACTIVE

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

    @Column(nullable = false, unique = true, length = 100)
    var email: String? = "",
    // email정보를 저장, 처음 생성시에는 null을 허용하나 DB에 저장될때는 NOT NULL이이기 때문에 유저에게는 반드시 email이 존재하여야 함

    @Column(nullable = false, unique = true, length = 30)
    var nickname: String? = "",
    // 유저 이름을 저장, email과 유사함

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String? = "",
    // 회원가입 시 Spring Security 해시값.

    @Column(nullable = false, length = 20)
    var status: String = "ACTIVE",
    // 회원 계정 상태. 가입 시 ACTIVE이며, 정지·탈퇴 처리에 사용함.
    // ACTIVE, BLOCKED, WITHDRAW로 나뉨

    @Column(name="email_verified_at")
    var emailVerifiedAt: LocalDateTime? = null,
    // 이메일 인증시각은 email_verified로 저장

    )