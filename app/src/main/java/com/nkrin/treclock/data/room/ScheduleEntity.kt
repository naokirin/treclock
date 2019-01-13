package com.nkrin.treclock.data.room

import android.arch.persistence.room.*

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var comment: String
)
