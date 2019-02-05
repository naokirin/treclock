package com.nkrin.treclock.util.mvvm

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import com.nkrin.treclock.util.rx.RxLauncher
import io.reactivex.disposables.Disposable

abstract class BaseViewModel : ViewModel() {

    private val launcher : RxLauncher = RxLauncher()

    fun launch(job: () -> Disposable) = launcher.launch(job)

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        launcher.dispose()
    }
}