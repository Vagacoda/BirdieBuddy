package com.birdiebuddy.birdiebuddyapi.domain.golfcourse.controller

import com.birdiebuddy.birdiebuddyapi.domain.golfcourse.GolfCourseResponse
import com.birdiebuddy.birdiebuddyapi.domain.golfcourse.service.GolfCourseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/golf-courses")
class GolfCourseController(
    private val golfCourseService: GolfCourseService
) {

    @GetMapping
    fun getGolfCourses(): List<GolfCourseResponse> {
        return golfCourseService.getGolfCourses()
    }
}