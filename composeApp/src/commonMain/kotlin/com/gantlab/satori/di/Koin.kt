package com.gantlab.satori.di

import com.gantlab.satori.AppViewModel
import com.gantlab.satori.db.SatoriRepository
import com.gantlab.satori.settings.SettingsManager
import com.gantlab.satori.getAnalytics
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule)
    }

// DLA IOS
fun initKoin() = initKoin {}

val commonModule = module {
    single { SettingsManager() }
    single { SatoriRepository(get()) }
    single { getAnalytics() }
    factory { AppViewModel(get(), get(), get()) }
}
