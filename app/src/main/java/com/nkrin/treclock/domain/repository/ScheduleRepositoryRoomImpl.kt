package com.nkrin.treclock.domain.repository

import com.nkrin.treclock.data.room.ScheduleDao
import com.nkrin.treclock.domain.entity.Schedule
import io.reactivex.Single
import com.nkrin.treclock.data.room.*

// ScheduleRepository implementation using ScheduleDao
class ScheduleRepositoryRoomImpl(private val scheduleDao: ScheduleDao) : ScheduleRepository {
    override fun getSchedules(): Single<List<Schedule>> {
        return scheduleDao.loadScheduleAndSteps().map { it.map { it.to() } }
    }

    override fun storeSchedules(schedules: List<Schedule>) {
        val scheduleAndStepsList = schedules.map { ScheduleAndSteps.from(it) }
        scheduleDao.upsertSchedules(scheduleAndStepsList.map { it.schedule })
        scheduleDao.upsertSteps(scheduleAndStepsList.flatMap { it.steps })

        val storedSchedules = getSchedules().blockingGet()
        val deletedScheduleIds = storedSchedules.map { it.id }.subtract(schedules.map { it.id }).toList()
        scheduleDao.deleteSchedulesFromId(deletedScheduleIds)
        val deletedStepIds = storedSchedules.flatMap { it.steps }.map { it.id }
            .subtract(schedules.flatMap { it.steps }.map { it.id }).toList()
        scheduleDao.deleteStepsFromId(deletedStepIds)
    }
}