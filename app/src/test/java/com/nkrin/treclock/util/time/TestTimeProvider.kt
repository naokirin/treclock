package com.nkrin.treclock.util.time

import com.nkrin.treclock.util.time.TimeProvider
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset


object TestTimeProvider : TimeProvider {
    override fun now(): OffsetDateTime =
        OffsetDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
}
