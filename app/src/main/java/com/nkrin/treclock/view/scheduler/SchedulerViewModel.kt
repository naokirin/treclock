package com.nkrin.treclock.view.scheduler

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.OnLifecycleEvent
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.rx.fromIo
import com.nkrin.treclock.util.rx.toUi
import java.util.*


class SchedulerViewModel(
    private val schedulerProvider: SchedulerProvider,
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

    val schedules: List<Schedule>
        get() { return schedulerRepository.getSchedulesFromCache() }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        load()
    }

    fun load() {
        _loadingEvents.value = Pending
        launch {
            schedulerRepository.getSchedules()
                .fromIo(schedulerProvider).toUi(schedulerProvider)
                .subscribe(
                    { onLoaded() },
                    { onLoadedError(it) }
                )
        }
    }

    fun addNewSchedule(title: String, comment: String): Int {
        _addingEvents.value = Pending
        val index = 0
        val lastIdSchedule = schedules.maxBy { it.id }
        val id = if (lastIdSchedule == null) 1 else lastIdSchedule.id + 1
        val list = schedules.toMutableList()
        list.add(index, Schedule(id, title, comment, mutableListOf()))
        launch {
            schedulerRepository.storeSchedules(list)
                .fromIo(schedulerProvider).toUi(schedulerProvider)
                .subscribe(
                    { _addingEvents.value = Success(index) },
                    {
                        list.removeAt(index)
                        _addingEvents.value = Error(it)
                    }
                )
        }

        return index
    }

    fun removeSchedule(id: Int): Int {
        _removingEvents.value = Pending
        val index = schedules.indexOfFirst { it.id == id }
        if (index != -1) {
            val list = schedules.toMutableList()
            launch {
                schedulerRepository.storeSchedules(list)
                    .fromIo(schedulerProvider).toUi(schedulerProvider)
                    .subscribe(
                        { _removingEvents.value = Success(index) },
                        { _removingEvents.value = Error(it) }
                    )
            }
        } else {
            _removingEvents.value = Error(NoSuchElementException())
        }
        return index
    }

    private fun onLoaded() {
        _loadingEvents.value = Success()
    }

    private fun onLoadedError(error: Throwable) {
        _loadingEvents.value = Error(error)
    }
}