package com.nkrin.treclock.util.time

import org.threeten.bp.Duration

inline fun <T> Iterable<T>.sumByDuration(selector: (T) -> Duration) : Duration =
        this.fold(Duration.ZERO) { d, t -> d + selector(t) }

