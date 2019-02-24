package com.nkrin.treclock.domain.repository

import com.nkrin.treclock.data.room.ScheduleDao
import com.nkrin.treclock.domain.entity.Schedule
import io.reactivex.Single
import com.nkrin.treclock.data.room.*
import io.reactivex.Completable

// ScheduleRepository implementation using ScheduleDao
class ScheduleRepositoryRoomImpl(private val scheduleDao: ScheduleDao) : ScheduleRepository {

    private var _cached: Boolean = false
    override val cached: Boolean
        get() = _cached
    private val _cache: MutableMap<Int, Schedule> = mutableMapOf()
    val cache: Map<Int, Schedule>
        get() = _cache

    override fun getSchedules(): Single<List<Schedule>> {
        return Single.create { emitter ->
            if (_cached) {
                emitter.onSuccess(_cache.values.toList())
                return@create
            }
            scheduleDao.loadScheduleAndSteps().map { schedules ->
                schedules.map { it.to() }
            }.subscribe(
                { schedules ->
                    _cache.clear()
                    _cache.putAll(schedules.associateBy({ it.id }, { it }))
                    _cached = true
                    emitter.onSuccess(schedules)
                },
                { emitter.onError(it) }
            )
        }
    }

    override fun getSchedule(id: Int): Single<Schedule> =
        getSchedules().map { schedules -> schedules.find { it.id == id } }

    override fun storeSchedules(schedules: List<Schedule>): Completable {
        return Completable.create { emitter ->
            try {
                val scheduleAndStepsList = schedules.map { ScheduleAndSteps.from(it) }
                scheduleDao.upsertSchedules(scheduleAndStepsList.map { it.schedule })
                scheduleDao.upsertSteps(scheduleAndStepsList.flatMap { it.steps })

                val storedSchedules = _cache.values
                val deletedScheduleIds = storedSchedules.map { it.id }
                    .subtract(schedules.map { it.id }).toList()
                scheduleDao.deleteSchedulesFromId(deletedScheduleIds)
                val deletedStepIds = storedSchedules.flatMap { it.steps }.map { it.id }
                    .subtract(schedules.flatMap { it.steps }.map { it.id }).toList()
                scheduleDao.deleteStepsFromId(deletedStepIds)
                _cache.clear()
                _cache.putAll(schedules.associateBy({ it.id }, { it }))
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
                val storedSchedules = _cache.values.filter { it.id == schedule.id }
                val deletedStepIds = storedSchedules.flatMap { it.steps }.map { it.id }
                    .subtract(scheduleAndSteps.steps.map { it.id }).toList()
                scheduleDao.deleteStepsFromId(deletedStepIds)
                _cache[schedule.id] = schedule
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
                scheduleIds.forEach { _cache.remove(it) }
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
                _cache.values.forEach { schedule ->
                    schedule.steps.removeAll { stepIds.contains(it.id) }
                }
                emitter.onComplete()
            } catch (e: Throwable) {
                emitter.onError(e)
            }
        }
    }

    override fun getSchedulesFromCache(): List<Schedule> {
        if (cached) {
            return cache.values.toList()
        }
        return listOf()
    }

    override fun getScheduleFromCache(id: Int): Schedule? {
        if (cached && cache.containsKey(id)) {
            return cache[id]
        }
        return null
    }
}