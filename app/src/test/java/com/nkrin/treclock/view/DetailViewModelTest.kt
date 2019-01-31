package com.nkrin.treclock.view

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.entity.Step
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.TestSchedulerProvider
import com.nkrin.treclock.util.capture
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.mvvm.ViewModelEvent
import com.nkrin.treclock.view.detail.DetailViewModel
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.mockito.*
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import java.time.Duration

class DetailViewModelTest: KoinTest {
    lateinit var viewModel: DetailViewModel

    @Mock
    lateinit var view: Observer<ViewModelEvent>

    @Mock
    lateinit var repository: ScheduleRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Captor
    lateinit var captor: ArgumentCaptor<Schedule>

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)

        given(repository.storeSchedule(com.nkrin.treclock.util.any())).willReturn(Completable.complete())

        viewModel = DetailViewModel(TestSchedulerProvider, repository)

        viewModel.loadingEvents.observeForever(view)
    }

    @Test
    fun testLoad() {
        val list = listOf(Schedule(1, "a", "", false, (mutableListOf(mock(Step::class.java)))))
        given(repository.getSchedules()).willReturn(Single.just(list))
        viewModel.loadSchedule(1)

        val arg = ArgumentCaptor.forClass(ViewModelEvent::class.java)
        verify(view, times(2)).onChanged(arg.capture())

        val values = arg.allValues
        assertEquals(2, values.size)
        assertTrue(values[0] is Pending)
        assertTrue(values[1] is Success)

        val success = values[1] as Success
        val actualSchedule = success.value as Schedule
        assertEquals(1, actualSchedule.steps.size)
    }

    @Test
    fun testAddStep() {
        val title = "title"
        val duration = Duration.ofMinutes(1)
        viewModel.schedule = Schedule(1, "schedule", "comment", false, mutableListOf())
        viewModel.addStep(title, duration)

        verify(repository, times(1)).storeSchedule(capture(captor))

        assertEquals(1, viewModel.schedule!!.steps.size)
        assertEquals(title, viewModel.schedule!!.steps[0].title)
        assertEquals(duration, viewModel.schedule!!.steps[0].duration)
    }
}