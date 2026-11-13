package com.birdiebuddy.birdiebuddyapi.domain.golfcourse

import java.math.BigDecimal

data class GolfCourseResponse(
    val id: Long,
    val name: String,
    val address: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal
)