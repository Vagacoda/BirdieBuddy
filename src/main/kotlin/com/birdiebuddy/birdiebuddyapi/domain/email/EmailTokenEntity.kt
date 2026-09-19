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
class EmailTokenEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity?= null,

    @Column(name = "target_email", nullable = false, length = 255)
    var targetEmail: String = "",

    @Column(nullable = false, length = 20)
    var purpose: String = "EMAIL_VERIFY",

    @Column(name = "token_hash", nullable = false, length = 255)
    var tokenHash: String = "",

    @Column(name = "expires_at", nullable = false)
    var expiresAt: LocalDateTime = LocalDateTime.now(),

    @Column(name="used_at")
    var usedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    )

interface UserRepository : JpaRepository<EmailTokenEntity, Long> {}