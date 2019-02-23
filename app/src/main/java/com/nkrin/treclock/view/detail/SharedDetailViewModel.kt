package com.nkrin.treclock.view.detail

import com.nkrin.treclock.util.mvvm.BaseViewModel
import com.nkrin.treclock.util.mvvm.SingleLiveEvent

class SharedDetailViewModel: BaseViewModel() {
    private val playingStepsCallbacks: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val stoppingStepsCallbacks: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val playingCallbacks: MutableMap<Int, () -> Unit> = mutableMapOf()
    private val stoppingCallbacks: MutableMap<Int, () -> Unit> = mutableMapOf()

    private val _removedStepEvent = SingleLiveEvent<Int>()
    val removedStepEvent: SingleLiveEvent<Int>
        get() = _removedStepEvent
    private val _changedStepEvent = SingleLiveEvent<Int>()
    val changedStepEvent: SingleLiveEvent<Int>
        get() = _changedStepEvent
    private val _insertedStepEvent = SingleLiveEvent<Int>()
    val insertedStepEvent: SingleLiveEvent<Int>
        get() = _insertedStepEvent

    private val _loadingEvent = SingleLiveEvent<Unit>()
    val loadingEvent: SingleLiveEvent<Unit>
        get() = _loadingEvent

    fun onPlayStep(id: Int) = playingStepsCallbacks.get(id)?.invoke()
    fun onPlay() = playingCallbacks.forEach { it.value.invoke() }
    fun onStopAllSteps() = stoppingStepsCallbacks.forEach { it.value.invoke() }
    fun onStop() = stoppingCallbacks.forEach { it.value.invoke() }

    fun addOnPlayStep(id: Int, callback: () -> Unit) {
        playingStepsCallbacks[id] = callback
    }
    fun addOnStopStep(id: Int, callback: () -> Unit) {
        stoppingStepsCallbacks[id] = callback
    }
    fun addOnPlay(id: Int, callback: () -> Unit) {
        playingCallbacks[id] = callback
    }
    fun addOnStop(id: Int, callback: () -> Unit) {
        stoppingCallbacks[id] = callback
    }

    fun removeOnPlayStep(id: Int) = playingStepsCallbacks.remove(id)
    fun removeOnStopStep(id: Int) = stoppingStepsCallbacks.remove(id)
    fun removeOnPlay(id: Int) = playingCallbacks.remove(id)
    fun removeOnStop(id: Int) = stoppingCallbacks.remove(id)

    fun onStepInserted(position: Int) { _insertedStepEvent.value = position }
    fun onStepRemoved(position: Int) { _removedStepEvent.value = position }
    fun onStepChanged(position: Int) { _changedStepEvent.value = position }

    fun onLoaded() { loadingEvent.value = Unit }
}