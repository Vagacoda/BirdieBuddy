package com.birdiebuddy.birdiebuddyapi.domain.role

import java.time.LocalDateTime

enum class BlockDuration {
    ONE_DAY, // 1일 차단
    ONE_WEEK, // 일주일 차단
    ONE_MONTH, // 한달 차단
    ONE_YEAR, // 1년 차단
    PERMANENT; // 영구 차단

    fun calculateBlockedUntil(now: LocalDateTime): LocalDateTime? {
        return when (this) {
            ONE_DAY -> now.plusDays(1)
            ONE_WEEK -> now.plusWeeks(1)
            ONE_MONTH -> now.plusMonths(1)
            ONE_YEAR -> now.plusYears(1)

            // 영구 차단은 끝나는 시각이 없으므로 null
            PERMANENT -> null
        }
    }
}