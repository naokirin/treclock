package com.nkrin.treclock.di

import android.arch.persistence.room.Room
import com.nkrin.treclock.data.room.ScheduleDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module.module

val roomDataSourceModule = module {

    // Room Database
    single {
        Room.databaseBuilder(androidApplication(), ScheduleDatabase::class.java, "schedule-db")
            .build()
    }

    single { get<ScheduleDatabase>().scheduleDao() }
}