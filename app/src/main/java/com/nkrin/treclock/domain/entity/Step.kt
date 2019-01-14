package com.nkrin.treclock.domain.entity

import java.time.OffsetDateTime

data class Step(
    var id: Int,
    var scheduleId: Int,
    var title: String,
    var start: OffsetDateTime? = null,
    var end: OffsetDateTime? = null,
    val actualStart: OffsetDateTime? = null,
    val actualEnd: OffsetDateTime? = null
)