package com.nkrin.treclock.data.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.nkrin.treclock.domain.entity.Step
import java.time.OffsetDateTime

@Entity(
    tableName = "steps",
    foreignKeys = [ForeignKey(
        entity = ScheduleEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("scheduleId"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["scheduleId"])]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var scheduleId: Int,
    var title: String,
    var start: OffsetDateTime? = null,
    var end: OffsetDateTime? = null,
    val actualStart: OffsetDateTime? = null,
    val actualEnd: OffsetDateTime? = null
) {
    fun to(): Step {
        return Step(this.id, this.scheduleId, this.title, this.start, this.end, this.actualStart, this.actualEnd)
    }

    companion object {
        fun from(step: Step): StepEntity {
            return StepEntity(step.id, step.scheduleId, step.title, step.start, step.end, step.actualStart, step.actualEnd)
        }
    }
}