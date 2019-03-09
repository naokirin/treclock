package com.nkrin.treclock.view.detail

import android.arch.lifecycle.*
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.rx.fromComputation
import com.nkrin.treclock.util.rx.fromIo
import com.nkrin.treclock.util.rx.toUi
import com.nkrin.treclock.util.time.TimeProvider
import io.reactivex.Completable
import io.reactivex.Observable
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

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

    private val _playingEvents = MutableLiveData<ViewModelEvent>()
    val playingEvents: LiveData<ViewModelEvent>
        get() = _playingEvents

    val playingStepEvents: LiveData<ViewModelEvent>
        get() = timers.playingStepEvents

    val settingStepTimerEvents: LiveData<ViewModelEvent>
        get() = timers.settingStepTimerEvents

    private val _tickingSecondsEvents = SingleLiveEvent<Unit>()
    val tickingSecondsEvents: LiveData<Unit>
        get() = _tickingSecondsEvents

    private var _scheduleId: Int = 0
    val scheduleId: Int
        get() = _scheduleId
    val schedule: Schedule?
        get() = schedulerRepository.getScheduleFromCache(scheduleId)

    private lateinit var timers : DetailPlayingTimers

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        launch {
            Observable.interval(1L, TimeUnit.SECONDS)
                .fromComputation(schedulerProvider).toUi(schedulerProvider)
                .subscribe {
                    _tickingSecondsEvents.value = Unit
                }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        loadSchedule()
    }

    fun initScheduleId(scheduleId: Int) {
        _scheduleId = scheduleId
        timers = DetailPlayingTimers(scheduleId, schedulerProvider, timeProvider, schedulerRepository) {
            playOrStopSchedule(false)
        }
    }

    fun loadSchedule() {
        _loadingEvents.value = Pending
        launch {
            schedulerRepository.getSchedule(scheduleId)
                .fromIo(schedulerProvider).toUi(schedulerProvider)
                .subscribe(
                    { schedule ->
                        _loadingEvents.value = Success(schedule)
                    },
                    { _loadingEvents.value = Error(it) }
                )
        }
    }

    fun addStep(title: String, duration: Duration) {
        val s = schedule
        if (s != null) {
            val index = s.steps.size
            val lastIdStep = s.steps.maxBy { it.id }
            val id = if (lastIdStep == null) s.id * 10000 + 1 else lastIdStep.id + 1
            s.steps.add(
                Step(id, s.id, index, title, duration, null)
            )
            _addingEvents.value = Pending
            launch {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _addingEvents.value = Success(index) },
                        { _addingEvents.value = Error(it) }
                    )
            }
        }
    }

    fun updateSchedule(title: String, comment: String) {
        val s = schedule
        if (s != null) {
            s.name = title
            s.comment = comment
            _updatingEvents.value = Pending
            launch {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _updatingEvents.value = Success() },
                        { _updatingEvents.value = Error(it) }
                    )
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
            s.steps.removeAt(index)
            launch {
                schedulerRepository.deleteStepsFromId(listOf(id))
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _removingEvents.value = Success(index) },
                        { _removingEvents.value = Error(it) }
                    )
            }
        }
    }

    fun updateStepOrder(id: Int, to: Int) {
        val s = schedule
        val steps = s?.steps
        if (steps != null) {
            val from = steps.indexOfFirst { it.id == id }
            val step = steps[from]
            steps.remove(step)
            steps.add(to, step)
            s.steps = steps
            launch {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe()
            }
        }
    }

    fun removeSchedule() {
        _removingScheduleEvents.value = Pending
        launch {
            schedulerRepository.deleteSchedulesFromId(listOf(scheduleId))
                .fromIo(schedulerProvider).toUi(schedulerProvider)
                .subscribe(
                    { _removingScheduleEvents.value = Success() },
                    { _removingScheduleEvents.value = Error(it) }
                )
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
            timers.startPlayingTimer(id)
            playOrStopSchedule(true, id)
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
        val s = schedule
        if (s != null) {
            s.steps.forEach { it.actualStart = null }
            launch {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe()
            }
        }
        else {
            _playingEvents.value = Error(Throwable("Schedule is not found"))
            return
        }

        timers.stopPlayingTimers()
        playOrStopSchedule(false)
    }

    private fun playOrStopSchedule(playing: Boolean, stepId: Int? = null) {
        launch {
            val s = schedule
            if (s != null) {
                s.steps.find { it.id == stepId }?.actualStart = timeProvider.now()
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

    fun resumePlaying() {
        timers.stopPlayingTimers()

        val now = timeProvider.now()
        val step = schedule?.steps?.reversed()?.firstOrNull {
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
                startStep(step.id)
                timers.startPlayingTimer(
                    step.id,
                    Duration.ofSeconds(now.toEpochSecond() - actualStart.toEpochSecond())
                )
            }
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
