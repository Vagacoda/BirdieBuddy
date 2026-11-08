package com.birdiebuddy.birdiebuddyapi.domain.image

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "uploaded_image")
class ImageEntity(
    // 이미지 고유 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 실제 파일 검사로 확인한 이미지 형식
    // 예: image/jpeg, image/png
    @Column(name = "content_type", nullable = false, length = 50)
    var contentType: String = "",

    // 실제 사진 데이터
    @Column(name = "image_data", nullable = false, columnDefinition = "LONGBLOB")
    var data: ByteArray = byteArrayOf(),

    // 업로드한 회원 번호
    @Column(name = "uploader_id", nullable = false)
    var uploaderId: Long = 0,

    // 업로드 시각
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

interface ImageRepository : JpaRepository<ImageEntity, Long>