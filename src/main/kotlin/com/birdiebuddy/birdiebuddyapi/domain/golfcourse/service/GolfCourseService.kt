package com.birdiebuddy.birdiebuddyapi.domain.golfcourse.service

import com.birdiebuddy.birdiebuddyapi.domain.golfcourse.GolfCourseRepository
import com.birdiebuddy.birdiebuddyapi.domain.golfcourse.GolfCourseResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GolfCourseService(
    private val golfCourseRepository: GolfCourseRepository
) {

    fun getGolfCourses(): List<GolfCourseResponse> {
        return golfCourseRepository.findAll().map { course ->
            GolfCourseResponse(
                id = course.id
                    ?: throw IllegalStateException("골프장 ID가 없습니다."),
                name = course.name,
                address = course.address,
                latitude = course.latitude,
                longitude = course.longitude
            )
        }
    }
}