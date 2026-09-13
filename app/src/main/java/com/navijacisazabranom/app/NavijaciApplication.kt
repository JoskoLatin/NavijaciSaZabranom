package com.navijacisazabranom.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.navijacisazabranom.app.notifikacije.NotifikacijaHelper
import com.navijacisazabranom.app.sync.IndeksiranjeScheduler
import com.navijacisazabranom.app.sync.SinkronizacijaScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NavijaciApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        NotifikacijaHelper(this).kreirajKanal()
        val workManager = WorkManager.getInstance(this)
        SinkronizacijaScheduler.zakazi(workManager)
        IndeksiranjeScheduler.zakazi(workManager)
    }
}
