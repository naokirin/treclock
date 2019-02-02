package com.nkrin.treclock.util.time

import java.time.OffsetDateTime

interface TimeProvider {
    fun now(): OffsetDateTime
}