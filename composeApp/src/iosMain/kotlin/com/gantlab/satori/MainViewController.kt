package com.gantlab.satori

import androidx.compose.ui.window.ComposeUIViewController
import com.gantlab.satori.db.DriverFactory

fun MainViewController() = ComposeUIViewController {
    val driverFactory = DriverFactory()
    App(driverFactory = driverFactory)
}
