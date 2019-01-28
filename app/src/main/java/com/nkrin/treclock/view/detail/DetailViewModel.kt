package com.nkrin.treclock.view.detail

import android.arch.lifecycle.LiveData
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.rx.fromIo
import com.nkrin.treclock.util.rx.toUi
import io.reactivex.Completable
import java.time.Duration

class DetailViewModel(
    private val schedulerProvider: SchedulerProvider,
    private val schedulerRepository: ScheduleRepository
): BaseViewModel() {

    private val _loadingEvents = SingleLiveEvent<ViewModelEvent>()
    val loadingEvents: LiveData<ViewModelEvent>
        get() = _loadingEvents

    private val _addingEvents = SingleLiveEvent<ViewModelEvent>()
    val addingEvents: LiveData<ViewModelEvent>
        get() = _addingEvents

    private val _removingEvents = SingleLiveEvent<ViewModelEvent>()
    val removingEvents: LiveData<ViewModelEvent>
        get() = _removingEvents

    private val _updatingOrderEvents = SingleLiveEvent<ViewModelEvent>()
    val updatingOrderEvents: LiveData<ViewModelEvent>
        get() = _updatingOrderEvents

    private val _storingEvents = SingleLiveEvent<ViewModelEvent>()
    val storingEvents: LiveData<ViewModelEvent>
        get() = _storingEvents

    private val _updatingEvents = SingleLiveEvent<ViewModelEvent>()
    val updatingEvents: LiveData<ViewModelEvent>
        get() = _updatingEvents

    private val _removingScheduleEvents = SingleLiveEvent<ViewModelEvent>()
    val removingScheduleEvents: LiveData<ViewModelEvent>
        get() = _removingScheduleEvents

    var schedule: Schedule? = null

    fun loadSchedule(scheduleId: Int) {
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

    fun storeSchedule() {
        val s = schedule
        if (s != null) {
            launch {
                schedulerRepository.storeSchedule(s)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _storingEvents.value = Success() },
                        { _storingEvents.value = Error(it) }
                    )
            }
        }
    }

    fun addStep(title: String, duration: Duration) {
        val s = schedule
        if (s != null) {
            val index = s.steps.size
            val lastIdStep = s.steps.maxBy { it.id }
            val id = if (lastIdStep == null) 1 else lastIdStep.id + 1
            schedule?.steps?.add(
                Step(id, s.id, index, title, duration, null, null)
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
            launch {
                val ss = schedule
                if (ss != null) {
                    schedulerRepository.storeSchedule(ss)
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
            _updatingOrderEvents.value = Pending
            launch {
                val ss = schedule
                if (ss != null) {
                    schedulerRepository.storeSchedule(ss)
                        .fromIo(schedulerProvider).toUi(schedulerProvider)
                        .subscribe(
                            { _updatingOrderEvents.value = Success(Pair(from, to)) },
                            { _updatingOrderEvents.value = Error(it) }
                        )
                } else {
                    Completable.complete().subscribe {
                        _updatingOrderEvents.value = Success()
                    }
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
}