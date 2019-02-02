package com.nkrin.treclock.util

import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class Timer(
    private val time: OffsetDateTime,
    private val fromScheduler: Scheduler,
    private val toScheduler: Scheduler,
    private val callback: () -> Unit) {

    private val disposables = CompositeDisposable()

    fun start(now: OffsetDateTime) {
        if (time > now) {
            disposables.add(
                Completable.timer(
                    time.toEpochSecond() - now.toEpochSecond(),
                    TimeUnit.SECONDS
                )
                    .subscribeOn(fromScheduler).observeOn(toScheduler)
                    .subscribe { callback() }
            )
        }
    }

    fun cancel() {
        disposables.clear()
    }
}