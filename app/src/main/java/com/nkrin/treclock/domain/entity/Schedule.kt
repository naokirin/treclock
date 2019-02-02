package com.nkrin.treclock.domain.entity

import java.time.OffsetDateTime

data class Schedule(
    var id: Int,
    var name: String,
    var comment: String,
    var steps: MutableList<Step>
) {
    fun played(now: OffsetDateTime) : Boolean {
        return steps.any {
            val start = it.actualStart
            return start != null && start + it.duration >= now
        }
    }
}