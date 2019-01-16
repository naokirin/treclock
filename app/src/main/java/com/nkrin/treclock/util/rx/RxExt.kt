package com.nkrin.treclock.util.rx

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun Completable.fromIo(schedulable: Schedulable): Completable =
    this.subscribeOn(schedulable.io())

fun <T> Single<T>.fromIo(schedulable: Schedulable): Single<T> =
    this.subscribeOn(schedulable.io())

fun Completable.toIo(schedulable: Schedulable): Completable =
    this.observeOn(schedulable.io())

fun <T> Single<T>.toIo(schedulable: Schedulable): Single<T> =
    this.observeOn(schedulable.io())


fun Completable.fromUi(schedulable: Schedulable): Completable =
    this.subscribeOn(schedulable.ui())

fun <T> Single<T>.fromUi(schedulable: Schedulable): Single<T> =
    this.subscribeOn(schedulable.ui())

fun Completable.toUi(schedulable: Schedulable): Completable =
    this.observeOn(schedulable.ui())

fun <T> Single<T>.toUi(schedulable: Schedulable): Single<T> =
    this.observeOn(schedulable.ui())


fun Completable.fromComputation(schedulable: Schedulable): Completable =
    this.subscribeOn(schedulable.computation())

fun <T> Single<T>.fromComputation(schedulable: Schedulable): Single<T> =
    this.subscribeOn(schedulable.computation())

fun Completable.toComputation(schedulable: Schedulable): Completable =
    this.observeOn(schedulable.computation())

fun <T> Single<T>.toComputation(schedulable: Schedulable): Single<T> =
    this.observeOn(schedulable.computation())