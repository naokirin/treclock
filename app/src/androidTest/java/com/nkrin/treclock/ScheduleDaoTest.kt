package com.nkrin.treclock

import android.support.test.runner.AndroidJUnit4
import com.nkrin.treclock.data.room.ScheduleDao
import com.nkrin.treclock.data.room.ScheduleDatabase
import com.nkrin.treclock.data.room.ScheduleEntity
import com.nkrin.treclock.data.room.StepEntity
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.time.OffsetDateTime

@RunWith(AndroidJUnit4::class)
class ScheduleDaoTest : KoinTest {

    private val scheduleDatabase: ScheduleDatabase by inject()
    private val scheduleDao: ScheduleDao by inject()

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
    fun testInsertSchedules() {
        val entities = listOf(
            ScheduleEntity(1, "test1", "comment1"),
            ScheduleEntity(2, "test2", "comment2")
        )

        scheduleDao.insertSchedules(entities)
        val requestedEntities = scheduleDao.loadScheduleAndSteps().blockingGet().map { it.schedule }

        assertEquals(entities, requestedEntities)
    }

    @Test
    fun testInsertSteps() {
        val entities = listOf(
            ScheduleEntity(1, "test1", "comment1"),
            ScheduleEntity(2, "test2", "comment2")
        )

        val stepEntities = listOf(
            StepEntity(1, 1, "step1", OffsetDateTime.now(), OffsetDateTime.now())
        )

        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        val requestedEntities = scheduleDao.loadSteps(listOf(1)).blockingGet()

        assertEquals(stepEntities, requestedEntities)
    }

    @Test
    fun testDeleteSchedules() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        scheduleDao.deleteSchedules(listOf(entities[0]))

        val requestedEntities = scheduleDao.loadScheduleAndSteps().blockingGet()

        assertEquals(entities[1], requestedEntities[0].schedule)
        assertTrue(requestedEntities[0].steps.isEmpty())
    }

    @Test
    fun testDeleteSteps() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        scheduleDao.deleteSteps(listOf(stepEntities[0]))

        val requestedEntities = scheduleDao.loadScheduleAndSteps().blockingGet()

        assertEquals(entities, requestedEntities.map{ it.schedule })
        assertEquals(listOf(stepEntities[1]), requestedEntities[0].steps)
    }

    @Test
    fun testUpdateSchedules() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        val updated = entities.map { it.also { it.comment = "updated" } }
        scheduleDao.updateSchedules(updated)

        val requestedEntities = scheduleDao.loadScheduleAndSteps().blockingGet()

        assertEquals(updated, requestedEntities.map{ it.schedule })
    }

    @Test
    fun testUpdateSteps() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        val updated = stepEntities.map { it.also { it.title = "updated" } }
        scheduleDao.updateSteps(updated)

        val requestedEntities = scheduleDao.loadScheduleAndSteps().blockingGet()

        assertEquals(updated, requestedEntities.flatMap{ it.steps })
    }

    @Test
    fun testUpsertSchedules() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        val inserted = ScheduleEntity(3, "test3", "comment3")
        val updated = entities.map { it.also { it.comment = "updated" } }
        val expected = updated + inserted
        scheduleDao.upsertSchedules(expected)

        val requestedEntities = scheduleDao.loadScheduleAndSteps().blockingGet()

        assertEquals(expected, requestedEntities.map{ it.schedule })
    }

    @Test
    fun testLoadScheduleAndSteps() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        val requestEntities = scheduleDao.loadScheduleAndSteps(listOf(1)).blockingGet()

        assertEquals(entities.filter { it.id == 1 }, requestEntities.map{ it.schedule })
        assertEquals(stepEntities, requestEntities.flatMap{ it.steps })
    }

    @Test
    fun testLoadSteps() {
        val entities = Factory.scheduleEntities()
        val stepEntities = Factory.stepEntities()
        scheduleDao.insertSchedules(entities)
        scheduleDao.insertSteps(stepEntities)

        val requestStepEntities = scheduleDao.loadSteps(listOf(1)).blockingGet()

        assertEquals(stepEntities.filter { it.id == 1 }, requestStepEntities)
    }

    object Factory {
        fun scheduleEntities(): List<ScheduleEntity> {
            return listOf(
                ScheduleEntity(1, "test1", "comment1"),
                ScheduleEntity(2, "test2", "comment2")
            )
        }

        fun stepEntities(): List<StepEntity> {
            return listOf(
                StepEntity(1, 1, "step1", OffsetDateTime.now(), OffsetDateTime.now()) ,
                StepEntity(2, 1, "step2", OffsetDateTime.now(), OffsetDateTime.now())
            )
        }
    }
}