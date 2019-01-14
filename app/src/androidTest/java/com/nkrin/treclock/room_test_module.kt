package com.nkrin.treclock

import android.arch.persistence.room.Room
import com.nkrin.treclock.data.room.ScheduleDatabase
import com.nkrin.treclock.domain.repository.ScheduleRepositoryRoomImpl
import org.koin.dsl.module.module

val roomTestModule = module(override = true) {
    single {
        Room.inMemoryDatabaseBuilder(get(), ScheduleDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    single {
        ScheduleRepositoryRoomImpl(get())
    }
}