package com.nkrin.treclock.domain.entity

import java.time.Duration
import java.time.OffsetDateTime

data class Step(
    var id: Int,
    var scheduleId: Int,
    var order: Int,
    var title: String,
    var duration: Duration,
    var actualStart: OffsetDateTime? = null,
    var actualEnd: OffsetDateTime? = null
)