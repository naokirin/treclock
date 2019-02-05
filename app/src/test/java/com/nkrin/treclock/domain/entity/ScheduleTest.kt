package com.nkrin.treclock.domain.entity

import com.nkrin.treclock.util.TestTimeProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class ScheduleTest {

    @Test
    fun testPlayedForEmptySteps() {
        val schedule = Schedule(1, "name", "comment", mutableListOf())
        assertFalse(schedule.played(TestTimeProvider.now()))
    }

    @Test
    fun testPlayedForNoStartedSteps() {
        val steps = mutableListOf(
            Step(1, 1, 1, "", Duration.ofMinutes(1), null)
        )
        val schedule = Schedule(1, "name", "comment", steps)
        assertFalse(schedule.played(TestTimeProvider.now()))
    }

    @Test
    fun testPlayedForStartedSteps() {
        val steps = mutableListOf(
            Step(1, 1, 1, "", Duration.ofMinutes(1), TestTimeProvider.now())
        )
        val schedule = Schedule(1, "name", "comment", steps)
        assertTrue(schedule.played(TestTimeProvider.now()))
        assertTrue(schedule.played(TestTimeProvider.now() + Duration.ofMinutes(1)))
        assertFalse(schedule.played((TestTimeProvider.now() + Duration.ofSeconds(61))))
    }
}