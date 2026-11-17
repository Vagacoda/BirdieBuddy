package com.birdiebuddy.birdiebuddyapi.domain.youtube

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

@Entity
@Table(name = "`Youtube`")
class YoutubeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 유튜브 URL에서 추출한 영상 ID
    @Column(name = "video_id", nullable = false, length = 20)
    var videoId: String = "",

    // 영상 제목
    @Column(nullable = false, length = 200)
    var title: String = "",

    // 채널 이름
    @Column(name = "channel_name", nullable = false, length = 200)
    var channelName: String = "",

    // 기존 설명 열은 유지. 값이 없으면 null
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    // 기존 영상 정보를 수정할 때 수정 시각 갱신
    @PreUpdate
    fun updateModifiedTime() {
        updatedAt = LocalDateTime.now()
    }
}

interface YoutubeRepository : JpaRepository<YoutubeEntity, Long> {

    // 같은 영상이 이미 등록돼 있는지 확인
    fun existsByVideoId(videoId: String): Boolean
}