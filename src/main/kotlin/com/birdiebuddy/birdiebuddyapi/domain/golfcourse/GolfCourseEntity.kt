package com.birdiebuddy.birdiebuddyapi.domain.golfcourse

import org.springframework.data.jpa.repository.JpaRepository
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name = "`GolfCourse`")
class GolfCourseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val name: String = "",

    @Column(nullable = false, length = 255)
    val address: String = "",

    @Column(nullable = false, precision = 10, scale = 7)
    val latitude: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 10, scale = 7)
    val longitude: BigDecimal = BigDecimal.ZERO
)
interface GolfCourseRepository : JpaRepository<GolfCourseEntity, Long>