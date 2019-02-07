package com.nkrin.treclock.util.time

import org.threeten.bp.OffsetDateTime

interface TimeProvider {
    fun now(): OffsetDateTime
}
