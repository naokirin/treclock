package com.nkrin.treclock.di

import org.koin.dsl.module.module

// App Components
val treclockAppModule = module { }

// Gather all app modules
val roomTreclockApp = listOf(treclockAppModule, roomDataSourceModule)