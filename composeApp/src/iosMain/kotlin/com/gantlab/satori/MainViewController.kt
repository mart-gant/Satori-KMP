package com.gantlab.satori

import androidx.compose.ui.window.ComposeUIViewController
import com.gantlab.satori.di.initKoinIos
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.notifications.IosNotificationManager

fun MainViewController(initialRoute: String? = null) = ComposeUIViewController {
    initKoinIos(
        factory = DriverFactory(),
        notifications = IosNotificationManager()
    )
    App(initialRoute = initialRoute)
}
