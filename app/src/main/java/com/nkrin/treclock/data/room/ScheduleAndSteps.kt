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
        val steps = steps.sortedBy { it.order }
        return Schedule(schedule.id, schedule.name, schedule.comment, schedule.played, steps.map { it.to() }.toMutableList())
    }

    companion object {
        fun from(schedule: Schedule): ScheduleAndSteps {
            val scheduleEntity = ScheduleEntity(schedule.id, schedule.name, schedule.comment, schedule.played)

            val stepEntities = schedule.steps.mapIndexed { index, step ->
                StepEntity(step.id, step.scheduleId, index, step.title, step.duration, step.actualStart)
            }

            return ScheduleAndSteps().also {
                it.schedule = scheduleEntity
                it.steps = stepEntities
            }
        }
    }
}