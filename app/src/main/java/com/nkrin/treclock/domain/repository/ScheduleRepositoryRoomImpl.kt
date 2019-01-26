package com.nkrin.treclock.domain.repository

import com.nkrin.treclock.data.room.ScheduleDao
import com.nkrin.treclock.domain.entity.Schedule
import io.reactivex.Single
import com.nkrin.treclock.data.room.*
import io.reactivex.Completable

// ScheduleRepository implementation using ScheduleDao
class ScheduleRepositoryRoomImpl(private val scheduleDao: ScheduleDao) : ScheduleRepository {

    override fun getSchedules(): Single<List<Schedule>> {
        return scheduleDao.loadScheduleAndSteps().map {
                schedules -> schedules.map { it.to() }
        }
    }

    override fun getSchedules(ids: List<Int>): Single<List<Schedule>> {
        return scheduleDao.loadScheduleAndSteps(ids).map {
                schedules -> schedules.map { it.to() }
        }
    }

    override fun storeSchedules(schedules: List<Schedule>): Completable {
        return Completable.create { emitter ->
            try {
                val scheduleAndStepsList = schedules.map { ScheduleAndSteps.from(it) }
                scheduleDao.upsertSchedules(scheduleAndStepsList.map { it.schedule })
                scheduleDao.upsertSteps(scheduleAndStepsList.flatMap { it.steps })

                val storedSchedules = getSchedules().blockingGet()
                val deletedScheduleIds = storedSchedules.map { it.id }
                    .subtract(schedules.map { it.id }).toList()
                scheduleDao.deleteSchedulesFromId(deletedScheduleIds)
                val deletedStepIds = storedSchedules.flatMap { it.steps }.map { it.id }
                    .subtract(schedules.flatMap { it.steps }.map { it.id }).toList()
                scheduleDao.deleteStepsFromId(deletedStepIds)
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }

    override fun storeSchedule(schedule: Schedule): Completable {
        return Completable.create { emitter ->
            try {
                val scheduleAndSteps = ScheduleAndSteps.from(schedule)
                scheduleDao.upsertSchedule(scheduleAndSteps.schedule)
                scheduleDao.upsertSteps(scheduleAndSteps.steps)
                val storedSchedules = getSchedules().blockingGet()
                val deletedStepIds = storedSchedules.flatMap { it.steps }.map { it.id }
                    .subtract(scheduleAndSteps.steps.map { it.id }).toList()
                scheduleDao.deleteStepsFromId(deletedStepIds)
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }

    override fun deleteSchedulesFromId(scheduleIds: List<Int>): Completable {
        return Completable.create { emitter ->
            try {
                scheduleDao.deleteSchedulesFromId(scheduleIds)
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }

    override fun deleteStepsFromId(stepIds: List<Int>): Completable {
        return Completable.create { emitter ->
            try {
                scheduleDao.deleteStepsFromId(stepIds)
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }
}