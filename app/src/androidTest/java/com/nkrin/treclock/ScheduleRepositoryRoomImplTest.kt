package com.nkrin.treclock

import android.support.test.runner.AndroidJUnit4
import com.nkrin.treclock.data.room.ScheduleDatabase
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.domain.repository.ScheduleRepositoryRoomImpl
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.time.Duration

@RunWith(AndroidJUnit4::class)
class ScheduleRepositoryRoomImplTest : KoinTest {

    private val scheduleDatabase: ScheduleDatabase by inject()
    private val scheduleRepositoryRoomImpl: ScheduleRepositoryRoomImpl by inject()

    @Before()
    fun before() {
        loadKoinModules(roomTestModule)
    }

    @After
    fun after() {
        scheduleDatabase.close()
        stopKoin()
    }

    @Test
    fun testSaveAndLoad() {
        val schedules = Factory.schedules()

        assertTrue(scheduleRepositoryRoomImpl.getSchedules().blockingGet().isEmpty())

        scheduleRepositoryRoomImpl.storeSchedules(schedules).blockingGet()

        assertEquals(schedules, scheduleRepositoryRoomImpl.getSchedules().blockingGet())
    }

    @Test
    fun testDelete() {
        val schedules = Factory.schedules()

        assertTrue(scheduleRepositoryRoomImpl.getSchedules().blockingGet().isEmpty())

        scheduleRepositoryRoomImpl.storeSchedules(schedules).blockingGet()

        val schedulesAfterDeleted = Factory.schedulesAfterDeleted()
        scheduleRepositoryRoomImpl.storeSchedules(schedulesAfterDeleted).blockingGet()

        assertEquals(schedulesAfterDeleted, scheduleRepositoryRoomImpl.getSchedules().blockingGet())
    }

    @Test
    fun testStoreSchedule() {
        val schedules = Factory.schedules()

        scheduleRepositoryRoomImpl.storeSchedule(schedules[0]).blockingGet()

        assertEquals(schedules[0], scheduleRepositoryRoomImpl.getSchedules().blockingGet()[0])
    }

    object Factory {
        fun schedules(): List<Schedule> {
            return listOf(
                Schedule(1, "test1", "comment1", false,
                    mutableListOf(Step(1, 1, 0, "", Duration.ofMinutes(10L), null))),
                Schedule(2, "test2", "comment2", false,
                    mutableListOf(Step(2, 2, 0, "", Duration.ofMinutes(10L), null)))
            )
        }

        fun schedulesAfterDeleted(): List<Schedule> {
            return listOf(Schedule(1, "test1", "comment1", false, mutableListOf()))
        }
    }}
