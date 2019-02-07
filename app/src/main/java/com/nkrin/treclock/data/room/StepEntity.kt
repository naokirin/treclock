package com.nkrin.treclock.data.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.nkrin.treclock.domain.entity.Step
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

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
    var order: Int,
    var title: String,
    var duration: Duration,
    val actualStart: OffsetDateTime? = null
) {
    fun to(): Step {
        return Step(this.id, this.scheduleId, this.order, this.title, this.duration, this.actualStart)
    }

    companion object {
        fun from(step: Step): StepEntity {
            return StepEntity(step.id, step.scheduleId, step.order, step.title, step.duration, step.actualStart)
        }
    }
}
