package com.nkrin.treclock.data.room

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import com.nkrin.treclock.domain.entity.Schedule

class ScheduleAndSteps {
    @Embedded
    lateinit var schedule: ScheduleEntity

    @Relation(parentColumn = "id", entityColumn = "scheduleId")
    lateinit var steps: List<StepEntity>

    fun to(): Schedule {
        val schedule = schedule
        val steps = steps
        return Schedule(schedule.id, schedule.name, schedule.comment, steps.map { it.to() })
    }

    companion object {
        fun from(schedule: Schedule): ScheduleAndSteps {
            val scheduleEntity = ScheduleEntity(schedule.id, schedule.name, schedule.comment)

            val stepEntities = schedule.steps.map {
                StepEntity(it.id, it.scheduleId, it.title, it.start, it.end, it.actualStart, it.actualEnd)
            }

            return ScheduleAndSteps().also {
                it.schedule = scheduleEntity
                it.steps = stepEntities
            }
        }
    }
}