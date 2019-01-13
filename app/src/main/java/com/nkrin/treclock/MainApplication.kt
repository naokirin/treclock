package com.nkrin.treclock

import android.app.Application
import com.nkrin.treclock.di.roomTreclockApp
import org.koin.android.ext.android.startKoin

/**
 * Main Application
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // start Koin context
        startKoin(this, roomTreclockApp)
    }
}