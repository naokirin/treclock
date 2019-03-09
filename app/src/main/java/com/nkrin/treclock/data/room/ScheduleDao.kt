package com.nkrin.treclock.data.room

import android.arch.persistence.room.*
import android.database.sqlite.SQLiteConstraintException
import io.reactivex.Single


@Dao
abstract class ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    abstract fun insertSchedules(schedules: List<ScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.FAIL)
    abstract fun insertSteps(steps: List<StepEntity>)

    @Delete
    abstract fun deleteSchedules(schedules: List<ScheduleEntity>)

    @Delete
    abstract fun deleteSteps(steps: List<StepEntity>)

    @Query("DELETE FROM schedules WHERE id IN(:scheduleIds)")
    abstract fun deleteSchedulesFromId(scheduleIds: List<Int>)

    @Query("DELETE FROM steps WHERE id IN(:stepIds)")
    abstract fun deleteStepsFromId(stepIds: List<Int>)

    @Update(onConflict = OnConflictStrategy.FAIL)
    abstract fun updateSchedules(schedules: List<ScheduleEntity>)

    @Update(onConflict = OnConflictStrategy.FAIL)
    abstract fun updateSteps(steps: List<StepEntity>)

    // Upsert schedules
    open fun upsertSchedules(schedules: List<ScheduleEntity>) {
        schedules.forEach { upsertSchedule(it) }
    }
    // Upsert a schedule
    open fun upsertSchedule(schedule: ScheduleEntity) {
        try {
            insertSchedules(listOf(schedule))
        } catch (exception: SQLiteConstraintException) {
            updateSchedules(listOf(schedule))
        }
    }

    // Upsert steps
    open fun upsertSteps(steps: List<StepEntity>) {
        steps.forEach { upsertStep(it) }
    }

    // Upsert a step
    open fun upsertStep(step: StepEntity) {
        try {
            insertSteps(listOf(step))
        } catch (exception: SQLiteConstraintException) {
            updateSteps(listOf(step))
        }
    }

    @Transaction
    @Query("SELECT * FROM schedules")
    abstract fun loadScheduleAndSteps(): Single<List<ScheduleAndSteps>>

    @Transaction
    @Query("SELECT * FROM schedules WHERE id IN(:scheduleIds)")
    abstract fun loadScheduleAndSteps(scheduleIds: List<Int>): Single<List<ScheduleAndSteps>>

    @Transaction
    @Query("SELECT * FROM steps WHERE id IN(:stepIds)")
    abstract fun loadSteps(stepIds: List<Int>): Single<List<StepEntity>>
}