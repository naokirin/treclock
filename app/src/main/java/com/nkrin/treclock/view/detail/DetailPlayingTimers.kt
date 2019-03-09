package com.nkrin.treclock.view.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.Timer
import com.nkrin.treclock.util.mvvm.SingleLiveEvent
import com.nkrin.treclock.util.mvvm.Success
import com.nkrin.treclock.util.mvvm.ViewModelEvent
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.time.TimeProvider
import org.threeten.bp.Duration

class DetailPlayingTimers(
    private val scheduleId: Int,
    private val schedulerProvider: SchedulerProvider,
    private val timeProvider: TimeProvider,
    private val repository: ScheduleRepository,
    private val onFinished: () -> Unit
    ) {
    private val schedule: Schedule?
        get() = repository.getScheduleFromCache(scheduleId)
    private val timers: MutableList<Timer> = mutableListOf()

    val settingStepTimerEvents = SingleLiveEvent<ViewModelEvent>()
    val playingStepEvents = MutableLiveData<ViewModelEvent>()

    fun startPlayingTimer(stepId: Int, offset: Duration = Duration.ZERO) {
        stopPlayingTimers()

        val index = schedule?.steps?.indexOfFirst { it.id == stepId }
        if (index != null && index != -1) {
            val now = timeProvider.now()
            val steps = schedule?.steps?.filterIndexed { i, _ -> i >= index }
            var allAmount = Duration.ZERO - offset
            val amounts = mutableListOf<Duration>()
            steps?.forEach { step ->
                amounts.add(allAmount)
                allAmount += step.duration
            }

            val resultSteps = steps?.zip(amounts)?.map { pair ->
                val step = pair.first
                val amount = pair.second
                step.createStarted(now + amount)
            }

            if (resultSteps != null) {
                schedule?.steps = resultSteps.toMutableList()
            }

            resultSteps?.forEachIndexed { i, step ->
                val actualStart = step.actualStart
                if (i == 0) {
                    playingStepEvents.value = Success(step.id)
                    settingStepTimerEvents.value = Success(Triple(step.title, step.duration, actualStart))
                } else if (actualStart != null) {
                    val timer = Timer(
                        actualStart,
                        schedulerProvider.computation(),
                        schedulerProvider.ui()
                    ) {
                        playingStepEvents.value = Success(step.id)
                    }
                    timers.add(timer)
                    timer.start(timeProvider.now())
                    settingStepTimerEvents.value = Success(Triple(step.title, step.duration, actualStart))
                }
            }

            val timer = Timer(
                now + allAmount,
                schedulerProvider.computation(),
                schedulerProvider.ui()
            ) {
                onFinished()
            }
            timers.add(timer)
            timer.start(timeProvider.now())
            settingStepTimerEvents.value = Success(Triple("終了", null, now + allAmount))
        }
    }

    fun stopPlayingTimers() {
        with(timers) {
            forEach { it.cancel() }
            clear()
        }
    }
}