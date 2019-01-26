package com.nkrin.treclock.view

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.nkrin.treclock.domain.entity.Schedule
import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.util.TestSchedulerProvider
import com.nkrin.treclock.util.capture
import com.nkrin.treclock.util.mvvm.*
import com.nkrin.treclock.util.mvvm.ViewModelEvent
import com.nkrin.treclock.view.scheduler.SchedulerViewModel
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

class SchedulerViewModelTest: KoinTest {
    lateinit var viewModel: SchedulerViewModel

    @Mock
    lateinit var view: Observer<ViewModelEvent>

    @Mock
    lateinit var repository: ScheduleRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Captor
    lateinit var captor: ArgumentCaptor<List<Schedule>>

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)

        given(repository.storeSchedules(com.nkrin.treclock.util.any())).willReturn(Completable.complete())

        viewModel = SchedulerViewModel(TestSchedulerProvider, repository)

        viewModel.loadingEvents.observeForever(view)
    }

    @Test
    fun testLoad() {
        val list = listOf(mock(Schedule::class.java))
        given(repository.getSchedules()).willReturn(Single.just(list))
        viewModel.load()

        val arg = ArgumentCaptor.forClass(ViewModelEvent::class.java)
        verify(view, times(2)).onChanged(arg.capture())

        val values = arg.allValues
        assertEquals(2, values.size)
        assertTrue(values[0] is Pending)
        assertTrue(values[1] is Success)
    }

    @Test
    fun testLoadFailed() {
        val error = Throwable("failed")
        given(repository.getSchedules()).willReturn(Single.error(error))
        viewModel.load()

        val arg = ArgumentCaptor.forClass(ViewModelEvent::class.java)
        verify(view, times(2)).onChanged(arg.capture())

        val values = arg.allValues
        assertEquals(2, values.size)
        assertTrue(values[0] is Pending)
        assertEquals(Error(error), values[1])
    }

    @Test
    fun testAddSchedule() {
        val title = "title"
        val comment = "comment"
        viewModel.addNewSchedule(title, comment)

        verify(repository, times(1)).storeSchedules(capture(captor))

        assertEquals(1, viewModel.list.size)
        assertEquals(title, viewModel.list[0].name)
        assertEquals(comment, viewModel.list[0].comment)
    }

    @Test
    fun testRemoveSchedule() {
        viewModel.addNewSchedule("", "")
        viewModel.removeSchedule(viewModel.list[0].id)

        verify(repository, times(2)).storeSchedules(capture(captor))

        assertTrue(viewModel.list.isEmpty())
    }
}