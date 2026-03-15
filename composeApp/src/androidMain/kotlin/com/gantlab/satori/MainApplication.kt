package com.gantlab.satori

import android.app.Application
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MainApplication)
            modules(module {
                single { DriverFactory(androidContext()) }
            })
        }
    }
}
