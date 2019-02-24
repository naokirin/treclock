package com.nkrin.treclock.domain.repository

import com.nkrin.treclock.domain.entity.Schedule
import io.reactivex.Completable
import io.reactivex.Single

// ScheduleRepository contract
interface ScheduleRepository {
    fun getSchedules(): Single<List<Schedule>>

    fun getSchedule(id: Int): Single<Schedule>

    fun storeSchedules(schedules: List<Schedule>): Completable

    fun storeSchedule(schedule: Schedule): Completable

    fun deleteSchedulesFromId(scheduleIds: List<Int>): Completable

    fun deleteStepsFromId(stepIds: List<Int>): Completable

    val cached : Boolean

    fun getSchedulesFromCache(): List<Schedule>

    fun getScheduleFromCache(id: Int): Schedule?
}
