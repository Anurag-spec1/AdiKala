package com.devshiv.adikala

import android.app.Application
import com.devshiv.adikala.database.AppDatabase
import com.devshiv.adikala.sync.BackgroundSyncManager

class AdikalaApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var backgroundSync: BackgroundSyncManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)

        backgroundSync = BackgroundSyncManager.getInstance(this)
        backgroundSync.startMonitoring()
    }

    override fun onTerminate() {
        super.onTerminate()
        backgroundSync.stop()
    }

    companion object {
        lateinit var instance: AdikalaApplication
            private set
    }
}