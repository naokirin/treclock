package com.nkrin.treclock.util.rx

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class RxLauncher : Disposable {
    private val disposables = CompositeDisposable()

    fun launch(job: () -> Disposable) = disposables.add(job())

    override fun dispose() = disposables.clear()

    override fun isDisposed(): Boolean = disposables.isDisposed
}