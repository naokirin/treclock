package com.nkrin.treclock.domain.entity

import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

data class Step(
    var id: Int,
    var scheduleId: Int,
    var order: Int,
    var title: String,
    var duration: Duration,
    var actualStart: OffsetDateTime? = null
) {
    fun createStarted(startTime: OffsetDateTime) =
            Step(id, scheduleId, order, title, duration, startTime)
}
