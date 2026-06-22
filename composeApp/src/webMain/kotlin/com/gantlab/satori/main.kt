package com.gantlab.satori

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.di.initKoin
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin {
        modules(module {
            single { DriverFactory() }
        })
    }

    ComposeViewport("root") {
        App()
    }
}
