package com.nkrin.treclock.domain.repository

import com.nkrin.treclock.domain.entity.Schedule
import io.reactivex.Single

// ScheduleRepository contract
interface ScheduleRepository {
    fun getSchedules(): Single<List<Schedule>>

    fun storeSchedules(schedules: List<Schedule>)
}
