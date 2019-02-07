package com.nkrin.treclock.view.detail

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.OnLifecycleEvent
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.Timer
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.rx.fromIo
import com.nkrin.treclock.util.rx.toUi
import com.nkrin.treclock.util.time.TimeProvider
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import org.threeten.bp.Duration

class DetailViewModel(
    private val schedulerProvider: SchedulerProvider,
    private val timeProvider: TimeProvider,
    private val schedulerRepository: ScheduleRepository
): BaseViewModel(), LifecycleObserver {

    private val _loadingEvents = SingleLiveEvent<ViewModelEvent>()
    val loadingEvents: LiveData<ViewModelEvent>
        get() = _loadingEvents

    private val _addingEvents = SingleLiveEvent<ViewModelEvent>()
    val addingEvents: LiveData<ViewModelEvent>
        get() = _addingEvents

    private val _removingEvents = SingleLiveEvent<ViewModelEvent>()
    val removingEvents: LiveData<ViewModelEvent>
        get() = _removingEvents

    private val _updatingEvents = SingleLiveEvent<ViewModelEvent>()
    val updatingEvents: LiveData<ViewModelEvent>
        get() = _updatingEvents

    private val _removingScheduleEvents = SingleLiveEvent<ViewModelEvent>()
    val removingScheduleEvents: LiveData<ViewModelEvent>
        get() = _removingScheduleEvents

    private val _playingEvents = SingleLiveEvent<ViewModelEvent>()
    val playingEvents: LiveData<ViewModelEvent>
        get() = _playingEvents

    private val _playingStepEvents = SingleLiveEvent<ViewModelEvent>()
    val playingStepEvents: LiveData<ViewModelEvent>
        get() = _playingStepEvents

    private val _settingStepTimerEvents = SingleLiveEvent<ViewModelEvent>()
    val settingStepTimerEvents: LiveData<ViewModelEvent>
        get() = _settingStepTimerEvents

    var scheduleId: Int = 0
    var schedule: Schedule? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        loadSchedule()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        storeSchedule()
    }

    fun loadSchedule() {
        _loadingEvents.value = Pending
        launch {
            schedulerRepository.getSchedules()
                .fromIo(schedulerProvider).toUi(schedulerProvider)
                .subscribe(
                    { schedules ->
                        schedule = schedules.find { it.id == scheduleId }
                        _loadingEvents.value = Success(schedule)
                    },
                    { _loadingEvents.value = Error(it) }
                )
        }
    }

    private fun storeSchedule() {
        val s = schedule
        if (s != null) {
            launch {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe()
            }
        }
    }

    fun addStep(title: String, duration: Duration) {
        val s = schedule
        if (s != null) {
            val index = s.steps.size
            val lastIdStep = s.steps.maxBy { it.id }
            val id = if (lastIdStep == null) s.id * 10000 + 1 else lastIdStep.id + 1
            schedule?.steps?.add(
                Step(id, s.id, index, title, duration, null)
            )
            _addingEvents.value = Pending
            launch {
                val ss = schedule
                if (ss != null) {
                    schedulerRepository.storeSchedule(ss)
                        .fromIo(schedulerProvider).toUi(schedulerProvider)
                        .subscribe(
                            { _addingEvents.value = Success(index) },
                            { _addingEvents.value = Error(it) }
                        )
                } else {
                    Completable.complete().subscribe {
                        _addingEvents.value = Success(-1)
                    }
                }
            }
        }
    }

    fun updateSchedule(title: String, comment: String) {
        val s = schedule
        if (s != null) {
            schedule?.name = title
            schedule?.comment = comment
            _updatingEvents.value = Pending
            launch {
                val ss = schedule
                if (ss != null) {
                    schedulerRepository.storeSchedule(ss)
                        .fromIo(schedulerProvider).toUi(schedulerProvider)
                        .subscribe(
                            { _updatingEvents.value = Success() },
                            { _updatingEvents.value = Error(it) }
                        )
                } else {
                    Completable.complete().subscribe {
                        _updatingEvents.value = Success(-1)
                    }
                }
            }
        }
    }

    fun updateStep(id: Int, title: String, duration: Duration) {
        val s = schedule
        if (s != null) {
            _updatingEvents.value = Pending
            val index = s.steps.indexOfFirst { it.id == id }
            s.steps[index].title = title
            s.steps[index].duration = duration
            updateStepForRepository(index)
        }
    }

    fun removeStep(id: Int) {
        val s = schedule
        if (s != null) {
            _removingEvents.value = Pending
            val index = s.steps.indexOfFirst { it.id == id }
            schedule?.steps?.removeAt(index)
            launch {
                val ss = schedule
                if (ss != null) {
                    schedulerRepository.deleteStepsFromId(listOf(id))
                        .fromIo(schedulerProvider).toUi(schedulerProvider)
                        .subscribe(
                            { _removingEvents.value = Success(index) },
                            { _removingEvents.value = Error(it) }
                        )
                } else {
                    Completable.complete().subscribe {
                        _removingEvents.value = Success(-1)
                    }
                }
            }
        }
    }

    fun updateStepOrder(id: Int, to: Int) {
        val steps = schedule?.steps
        if (steps != null) {
            val from = steps.indexOfFirst { it.id == id }
            val step = steps[from]
            steps.remove(step)
            steps.add(to, step)
            schedule?.steps = steps
            launch {
                val ss = schedule
                if (ss != null) {
                    schedulerRepository.storeSchedule(ss)
                        .fromIo(schedulerProvider).toUi(schedulerProvider)
                        .subscribe()
                } else {
                    CompositeDisposable()
                }
            }
        }
    }

    fun removeSchedule() {
        val s = schedule
        if (s != null) {
            schedule = null
            val id = s.id
            _removingScheduleEvents.value = Pending
            launch {
                schedulerRepository.deleteSchedulesFromId(listOf(id))
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _removingScheduleEvents.value = Success() },
                        { _removingScheduleEvents.value = Error(it) }
                    )
            }
        }
    }

    fun startStep(id: Int) {
        _playingEvents.value = Pending
        val index = schedule?.steps?.indexOfFirst { it.id == id }
        if (index != null && index >= 0) {
            if (schedule == null) {
                _playingEvents.value = Error(Throwable("Schedule is not found"))
                return
            }
            if (schedule?.played(timeProvider.now()) == true) {
                schedule?.steps?.forEach {
                    it.actualStart = null
                }
            }
            startPlayingTimer(id)
            playOrStopSchedule(true)
        }
    }

    fun startSchedule() {
        val s = schedule
        if (s != null) {
            val step = s.steps[0]
            startStep(step.id)
        } else {
            _playingEvents.value = Pending
            _playingEvents.value = Error(Throwable("Schedule is not found"))
        }
    }

    fun stopSchedule() {
        _playingEvents.value = Pending
        schedule?.steps?.forEach { it.actualStart = null }

        if (schedule == null) {
            _playingEvents.value = Error(Throwable("Schedule is not found"))
            return
        }

        stopPlayingTimers()
        playOrStopSchedule(false)
    }

    private fun playOrStopSchedule(playing: Boolean) {
        launch {
            val s = schedule
            if (s != null) {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _playingEvents.value = Success(playing) },
                        { _playingEvents.value = Error(it) }
                    )
            } else {
                Completable.complete().subscribe {
                    _playingEvents.value = Success()
                }
            }
        }
    }

    fun resumePlayingTimer() {
        stopPlayingTimers()

        val now = timeProvider.now()
        val step = schedule?.steps?.lastOrNull {
            val actualStart = it.actualStart
            actualStart != null && actualStart <= now
        }
        if (schedule?.steps.isNullOrEmpty()) {
            return
        }

        if (step != null) {
            val last = schedule?.steps?.last()
            val lastActualStart = last?.actualStart
            if (lastActualStart != null) {
                if (now > lastActualStart + last.duration) {
                    stopSchedule()
                    return
                }
            }
            val actualStart = step.actualStart
            if (actualStart != null) {
                startPlayingTimer(
                    step.id,
                    Duration.ofSeconds(now.toEpochSecond() - actualStart.toEpochSecond())
                )
            }
        }
    }

    private val timers: MutableList<Timer> = mutableListOf()

    private fun startPlayingTimer(stepId: Int, offset: Duration = Duration.ZERO) {
        stopPlayingTimers()

        val index = schedule?.steps?.indexOfFirst { it.id == stepId }
        if (index != null && index != -1) {
            val now = timeProvider.now()
            val steps = schedule?.steps?.filterIndexed { i, _ -> i >= index }
            var amount = Duration.ZERO - offset

            steps?.forEachIndexed { i, step ->
                if (i == 0) {
                    step.actualStart = now + amount
                    _playingStepEvents.value = Success(step.id)
                    _settingStepTimerEvents.value = Success(Triple(step.title, step.duration, now + amount))
                } else {
                    step.actualStart = now + amount
                    val timer = Timer(
                        now + amount,
                        schedulerProvider.computation(),
                        schedulerProvider.ui()
                    ) {
                        _playingStepEvents.value = Success(step.id)
                    }
                    timers.add(timer)
                    timer.start(timeProvider.now())
                    _settingStepTimerEvents.value = Success(Triple(step.title, step.duration, now + amount))
                }
                amount += step.duration
            }
            val timer = Timer(
                now + amount,
                schedulerProvider.computation(),
                schedulerProvider.ui()
            ) {
                playOrStopSchedule(false)
            }
            timers.add(timer)
            timer.start(timeProvider.now())
            _settingStepTimerEvents.value = Success(Triple("終了", null, now + amount))
        }
    }

    private fun stopPlayingTimers() {
        with(timers) {
            forEach { it.cancel() }
            clear()
        }
    }

    private fun updateStepForRepository(index: Int) {
        launch {
            val s = schedule
            if (s != null) {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _updatingEvents.value = Success(index) },
                        { _updatingEvents.value = Error(it) }
                    )
            } else {
                Completable.complete().subscribe {
                    _updatingEvents.value = Success(-1)
                }
            }
        }
    }
}
