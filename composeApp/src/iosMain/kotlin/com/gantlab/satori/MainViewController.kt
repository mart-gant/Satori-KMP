package com.gantlab.satori

import androidx.compose.ui.window.ComposeUIViewController
import com.gantlab.satori.di.initKoinIos
import com.gantlab.satori.db.DriverFactory

fun MainViewController() = ComposeUIViewController {
    initKoinIos(DriverFactory())
    App()
}
