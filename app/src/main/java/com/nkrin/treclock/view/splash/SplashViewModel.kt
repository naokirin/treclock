package com.nkrin.treclock.view.splash

import android.arch.lifecycle.LiveData
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.rx.Schedulable
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class SplashViewModel(private val schedulable: Schedulable): BaseViewModel() {

    private val _events = SingleLiveEvent<ViewModelEvent>()
    val events: LiveData<ViewModelEvent>
        get() = _events

    fun load() {
        launch {
            Observable.timer(2400, TimeUnit.MILLISECONDS).observeOn(schedulable.ui())
                .subscribe(
                    { _events.value = Success },
                    { error -> _events.value = Error(error) })
        }
    }
}