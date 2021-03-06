package com.nkrin.treclock.domain.entity

import org.threeten.bp.OffsetDateTime

data class Schedule(
    var id: Int,
    var name: String,
    var comment: String,
    var steps: MutableList<Step>
) {
    fun played(now: OffsetDateTime) : Boolean {
        return steps.reversed().any {
            val start = it.actualStart
            return start != null && (start + it.duration) >= now
        }
    }

    fun playingStep(now: OffsetDateTime) : Step? = steps.firstOrNull {
        val actualStart = it.actualStart
        if (actualStart != null) {
            actualStart <= now && now < actualStart + it.duration
        } else {
            false
        }
    }
}
