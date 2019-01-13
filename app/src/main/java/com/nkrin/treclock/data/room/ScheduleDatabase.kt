package com.nkrin.treclock.data.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = [ScheduleEntity::class, StepEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class ScheduleDatabase: RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
}