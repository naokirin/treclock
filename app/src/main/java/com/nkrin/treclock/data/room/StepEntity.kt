package com.nkrin.treclock.data.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
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
)