package com.nkrin.treclock.util.rx

import io.reactivex.Scheduler

interface Schedulable {
    fun io(): Scheduler
    fun ui(): Scheduler
    fun computation(): Scheduler
}