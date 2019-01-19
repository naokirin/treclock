package com.nkrin.treclock.util

import com.nkrin.treclock.util.rx.SchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

object TestSchedulerProvider: SchedulerProvider {
    override fun io(): Scheduler = Schedulers.trampoline()
    override fun ui(): Scheduler = Schedulers.trampoline()
    override fun computation(): Scheduler = Schedulers.trampoline()
}