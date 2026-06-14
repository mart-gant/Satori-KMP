package com.gantlab.satori

import android.app.Application
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.di.initKoin
import com.gantlab.satori.notifications.AndroidNotificationManager
import com.gantlab.satori.notifications.NotificationManager
import com.gantlab.satori.worker.SyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        initKoin {
            androidContext(this@MainApplication)
            workManagerFactory()
            modules(module {
                single { DriverFactory(androidContext()) }
                single<NotificationManager> { AndroidNotificationManager(androidContext()) }
                worker { SyncWorker(get(), get(), get()) }
            })
        }
    }
}
