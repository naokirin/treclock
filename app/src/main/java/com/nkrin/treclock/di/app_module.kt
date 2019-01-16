package com.nkrin.treclock.di

import com.nkrin.treclock.util.rx.AndroidSchedulable
import com.nkrin.treclock.util.rx.Schedulable
import com.nkrin.treclock.view.splash.SplashViewModel
import org.koin.android.viewmodel.experimental.builder.viewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

// App Components
val treclockAppModule = module {
    single<Schedulable> { AndroidSchedulable() }

    viewModel { SplashViewModel(get()) }
}

// Gather all app modules
val roomTreclockApp = listOf(treclockAppModule, roomDataSourceModule)