package com.gantlab.satori.di

import com.gantlab.satori.AppViewModel
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.getAnalytics
import com.gantlab.satori.Analytics
import com.gantlab.satori.db.DriverFactory
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.koin.core.module.Module
import org.koin.core.KoinApplication

val commonModule: Module = module {
    single<SettingsManager> { SettingsManager() }
    single<SatoriRepository> { 
        val factory: DriverFactory = get()
        SatoriRepository(driverFactory = factory) 
    }
    single<Analytics> { getAnalytics() }
    factory<AppViewModel> { 
        val repo: SatoriRepository = get()
        val sets: SettingsManager = get()
        val an: Analytics = get()
        AppViewModel(repo, sets, an) 
    }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

/**
 * iOS-specific initialization that provides the platform DriverFactory
 */
fun initKoinIos(factory: DriverFactory): KoinApplication = initKoin {
    modules(module {
        single<DriverFactory> { factory }
    })
}
