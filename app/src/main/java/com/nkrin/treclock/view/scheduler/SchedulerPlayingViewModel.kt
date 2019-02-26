package com.nkrin.treclock.view.scheduler

import android.arch.lifecycle.LifecycleObserver
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.Timer
import com.nkrin.treclock.util.mvvm.BaseViewModel
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.time.TimeProvider

class SchedulerPlayingViewModel(
    private val schedulerProvider: SchedulerProvider,
    private val schedulerRepository: ScheduleRepository,
    private val timeProvider: TimeProvider
): BaseViewModel(), LifecycleObserver {
    private val timers: MutableMap<Int, Timer> = mutableMapOf()

    fun stopSchedule(scheduleId: Int) {
        timers[scheduleId]?.cancel()
        timers.remove(scheduleId)
    }

    fun resumeSchedule(scheduleId: Int, onStop: () -> Unit) {
        if (!schedulerRepository.cached) {
            return
        }
        val schedule = schedulerRepository.getScheduleFromCache(scheduleId) ?: return
        val lastIndex = schedule.steps.indexOfLast { it.actualStart != null }
        if (lastIndex >= 0) {
            val lastTime = schedule.steps.takeLast(schedule.steps.size - lastIndex)
                .fold(schedule.steps[lastIndex].actualStart!!) { acc, step -> acc + step.duration }
            val timer = Timer(lastTime, schedulerProvider.computation(), schedulerProvider.ui()) {
                onStop()
                timers.remove(scheduleId)
            }
            timer.start(timeProvider.now())
            timers[schedule.id] = timer
        }
    }
}