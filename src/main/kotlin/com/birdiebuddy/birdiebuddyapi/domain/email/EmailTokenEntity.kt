package com.birdiebuddy.birdiebuddyapi.domain.email

import com.birdiebuddy.birdiebuddyapi.domain.user.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

@Entity
@Table(name = "`EmailToken`")
// DB의 EmailToken과 연결
class EmailTokenEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    // EmailToken의 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity?= null,
    // 인증번호가 한 회원에게 여러번 갈수있으므로 다대일임
    // 2026/10/01 - 21:04
    // 기존에는 인증번호를 만들기 전에 회원을 먼저 저장
    // User 생성
    //→ UserEntity 존재
    //→ EmailToken.user_id에 회원 ID 저장
    // 따라서 user_id가 반드시 있어야함
    // @JoinColumn(name = "user_id", nullable = false)
    // var user: UserEntity? = null

    // 하지만 인증번호 보내고 인증번호확인하고 그 뒤 User생성 방식으로 변경하였음
    // 이메일 입력
    // → 인증번호 발송
    // → 인증번호 확인
    // → 그 뒤 User 생성
    // nullable = false는 id가 null일수 있다고 알려주기위함임.
    // 즉, 회원가입 하기 전까지는 해당 email을 가진 user는 null인 상태이기 때문에 user정보는 null일수있다고 알리는  것임

    @Column(name = "target_email", nullable = false, length = 254)
    var targetEmail: String = "",
    // 인증번호를 보낼 이메일 주소

    @Column(nullable = false, length = 20)
    var purpose: String = "EMAIL_VERIFY",
    // 인증번호 용도의 구분

    @Column(name = "token_hash", nullable = false, length = 255)
    var tokenHash: String = "",
    // 실제 인증번호를 저장하지 않고 해시값을 저장

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now(),
    // 인증번호 만료 시각

    @Column(name="used_at")
    var usedAt: LocalDateTime? = null,
    // 사용되지 않은 인증번호는 null, 인증 성공시 현재시각 넣음

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    //인증번호 생성 시각
    )

// 2026/09/20 - 14:00 인증번호 조회
interface EmailTokenRepository : JpaRepository<EmailTokenEntity, Long> {
    fun findFirstByTargetEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
        // 함수 이름 자체가 조회조건, JPA가 함수 이름을 해석해 조회 SQL을 자동 생성
        //findFirstBy
        //→ 조건에 맞는 행 중 하나를 찾음
        //TargetEmailAndPurposeAndUsedAtIsNull
        //→ 이메일이 같고
        //→ 용도가 EMAIL_VERIFY이고
        //→ 아직 사용하지 않은 used_at = NULL인 행
        //OrderByCreatedAtDesc
        //→ 생성 시각 최신순으로 정렬
        //→ 가장 최근에 발급한 인증번호 한 건을 선택
        targetEmail: String,
        purpose: String
    ): EmailTokenEntity?
}