package com.nkrin.treclock.util.time

import org.threeten.bp.OffsetDateTime

class ActualTimeProvider: TimeProvider {
    override fun now() : OffsetDateTime = OffsetDateTime.now()
}
