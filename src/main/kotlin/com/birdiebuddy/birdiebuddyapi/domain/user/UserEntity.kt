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
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

// 2026/09/17 - 20:12 UserEntity 생성
@Entity
@Table(name = "`User`")
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
    var email: String = "",
    // email정보를 저장, 처음 생성시에는 null을 허용하나 DB에 저장될때는 NOT NULL이이기 때문에 유저에게는 반드시 email이 존재하여야 함

    @Column(nullable = false, unique = true, length = 30)
    var nickname: String = "",
    // 유저 이름을 저장, email과 유사함

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",
    // 회원가입 시 Spring Security 해시값.

    @Column(nullable = false, length = 20)
    var status: String = "ACTIVE",
    // 회원 계정 상태. 가입 시 ACTIVE이며, 정지·탈퇴 처리에 사용함.
    // ACTIVE, BLOCKED, WITHDRAWN으로 구분

    @Column(name="email_verified_at")
    var emailVerifiedAt: LocalDateTime? = null,
    // 이메일 인증이 완료된 시각을 email_verified_at에 저장

    @Column(name = "blocked_until")
    var blockedUntil: LocalDateTime? = null,
    // 유저 정지 시각을 blocked_until에 저장.
    // 정상 회원과 영구 정지 회원은 null.

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    // 유저 회원가입 시각, 바뀌면 안되기 때문에 updatable = false임

    @Column(name = "updated_at" , nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    // 유저의 유저정보 수정 시각, 처음은 유저 회원가입 시각이며 이후 수정될때마다 갱신

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
    // 유저 회원탈퇴시각, 회원탈퇴할때만 시간 저장.
    ){
    @PreUpdate
    fun updatedModifiedTime(){
        updatedAt = LocalDateTime.now()
    }
}

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun existsByEmail(email: String): Boolean
    // 이미 가입된 이메일이면 true, 아니면 false
    fun existsByNickname(nickname: String): Boolean
    // 이메일 회원 한명의 전체정보 조회
    fun findByEmail(email: String): UserEntity?
    // 회원이 있으면 UserEntity 객체 반환, 아니면 null반환
}