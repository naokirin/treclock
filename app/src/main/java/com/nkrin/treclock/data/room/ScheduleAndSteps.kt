package com.nkrin.treclock.data.room

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation

class ScheduleAndSteps {
    @Embedded
    lateinit var schedule: ScheduleEntity

    @Relation(parentColumn = "id", entityColumn = "scheduleId")
    lateinit var steps: List<StepEntity>
}