package com.nkrin.treclock.util

import com.nkrin.treclock.util.time.TimeProvider
import java.time.OffsetDateTime
import java.time.ZoneOffset


object TestTimeProvider : TimeProvider {
    override fun now(): OffsetDateTime =
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
}