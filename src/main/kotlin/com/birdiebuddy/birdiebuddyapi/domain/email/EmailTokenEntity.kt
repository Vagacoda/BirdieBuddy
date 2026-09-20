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
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity?= null,
    // 인증번호가 한 회원에게 여러번 갈수있으므로 다대일임

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

interface EmailTokenRepository : JpaRepository<EmailTokenEntity, Long> {
    fun findFirstByTargetEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
        // 함수 이름 자체가 조회조건(Spring Data JPA제공)
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