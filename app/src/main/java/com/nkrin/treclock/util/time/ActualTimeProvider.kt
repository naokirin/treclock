package com.nkrin.treclock.util.time

import java.time.OffsetDateTime

class ActualTimeProvider: TimeProvider {
    override fun now() : OffsetDateTime = OffsetDateTime.now()
}