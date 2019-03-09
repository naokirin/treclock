package com.nkrin.treclock.util.time

import org.junit.Assert.*
import org.junit.Test
import org.threeten.bp.Duration

class DetailViewModelTest {

    @Test
    fun `sumByDuration with empty returns zero`() {
        assertTrue(listOf<Duration>().sumByDuration { it }.isZero)
    }

    @Test
    fun `sumByDuration with an element list returns the element`() {
        val duration = Duration.ofSeconds(1)
        assertEquals(duration, listOf<Duration>(duration).sumByDuration { it })
    }

    @Test
    fun `sumByDuration with mutliple elements returns adding all elements`() {
        val duration1 = Duration.ofSeconds(1)
        val duration2 = Duration.ofSeconds(2)
        val duration3 = Duration.ofSeconds(3)
        val result = duration1 + duration2 + duration3
        val list = listOf(duration1, duration2, duration3)
        assertEquals(result, list.sumByDuration { it })
    }
}
