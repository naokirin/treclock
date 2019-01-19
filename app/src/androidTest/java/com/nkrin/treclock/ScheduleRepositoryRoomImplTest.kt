package com.nkrin.treclock

import android.support.test.runner.AndroidJUnit4
import com.nkrin.treclock.data.room.ScheduleDao
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
import java.time.OffsetDateTime

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

        scheduleRepositoryRoomImpl.storeSchedules(schedules)

        assertEquals(schedules, scheduleRepositoryRoomImpl.getSchedules().blockingGet())
    }

    @Test
    fun testDelete() {
        val schedules = Factory.schedules()

        assertTrue(scheduleRepositoryRoomImpl.getSchedules().blockingGet().isEmpty())

        scheduleRepositoryRoomImpl.storeSchedules(schedules)

        val schedulesAfterDeleted = Factory.schedulesAfterDeleted()
        scheduleRepositoryRoomImpl.storeSchedules(schedulesAfterDeleted)

        assertEquals(schedulesAfterDeleted, scheduleRepositoryRoomImpl.getSchedules().blockingGet())
    }

    object Factory {
        fun schedules(): List<Schedule> {
            val now = OffsetDateTime.now()
            return listOf(
                Schedule(1, "test1", "comment1",
                    listOf(Step(1, 1, "", now, now, null, null))),
                Schedule(2, "test2", "comment2",
                    listOf(Step(2, 2, "", now, now, null, null)))
            )
        }

        fun schedulesAfterDeleted(): List<Schedule> {
            return listOf(Schedule(1, "test1", "comment1", listOf()))
        }
    }}
