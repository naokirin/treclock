package com.nkrin.treclock.util.rx

import io.reactivex.Completable
import io.reactivex.Single

fun Completable.fromIo(schedulerProvider: SchedulerProvider): Completable =
    this.subscribeOn(schedulerProvider.io())

fun <T> Single<T>.fromIo(schedulerProvider: SchedulerProvider): Single<T> =
    this.subscribeOn(schedulerProvider.io())

fun Completable.toIo(schedulerProvider: SchedulerProvider): Completable =
    this.observeOn(schedulerProvider.io())

fun <T> Single<T>.toIo(schedulerProvider: SchedulerProvider): Single<T> =
    this.observeOn(schedulerProvider.io())


fun Completable.fromUi(schedulerProvider: SchedulerProvider): Completable =
    this.subscribeOn(schedulerProvider.ui())

fun <T> Single<T>.fromUi(schedulerProvider: SchedulerProvider): Single<T> =
    this.subscribeOn(schedulerProvider.ui())

fun Completable.toUi(schedulerProvider: SchedulerProvider): Completable =
    this.observeOn(schedulerProvider.ui())

fun <T> Single<T>.toUi(schedulerProvider: SchedulerProvider): Single<T> =
    this.observeOn(schedulerProvider.ui())


fun Completable.fromComputation(schedulerProvider: SchedulerProvider): Completable =
    this.subscribeOn(schedulerProvider.computation())

fun <T> Single<T>.fromComputation(schedulerProvider: SchedulerProvider): Single<T> =
    this.subscribeOn(schedulerProvider.computation())

fun Completable.toComputation(schedulerProvider: SchedulerProvider): Completable =
    this.observeOn(schedulerProvider.computation())

fun <T> Single<T>.toComputation(schedulerProvider: SchedulerProvider): Single<T> =
    this.observeOn(schedulerProvider.computation())