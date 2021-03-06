package com.nkrin.treclock.di

import com.nkrin.treclock.domain.repository.ScheduleRepository
import com.nkrin.treclock.domain.repository.ScheduleRepositoryRoomImpl
import com.nkrin.treclock.util.rx.AndroidSchedulerProvider
import com.nkrin.treclock.util.rx.SchedulerProvider
import com.nkrin.treclock.util.time.ActualTimeProvider
import com.nkrin.treclock.util.time.TimeProvider
import com.nkrin.treclock.view.detail.DetailViewModel
import com.nkrin.treclock.view.detail.SharedDetailViewModel
import com.nkrin.treclock.view.scheduler.SchedulerPlayingViewModel
import com.nkrin.treclock.view.scheduler.SchedulerViewModel
import com.nkrin.treclock.view.splash.SplashViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

// App Components
val treclockAppModule = module {
    single<SchedulerProvider> { AndroidSchedulerProvider() }
    single<ScheduleRepository> { ScheduleRepositoryRoomImpl(get()) }
    single<TimeProvider> { ActualTimeProvider() }

    viewModel { SplashViewModel(get()) }
    viewModel { SchedulerViewModel(get(), get()) }
    viewModel { SchedulerPlayingViewModel(get(), get(), get()) }
    viewModel { DetailViewModel(get(), get(), get()) }
    viewModel { SharedDetailViewModel() }
}

// Gather all app modules
val roomTreclockApp = listOf(treclockAppModule, roomDataSourceModule)