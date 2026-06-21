package com.gantlab.satori

import androidx.compose.ui.window.ComposeUIViewController
import com.gantlab.satori.db.*
import com.gantlab.satori.notifications.IosNotificationManager

fun MainViewController(initialRoute: String? = null) = ComposeUIViewController {
    // iOS logic for RealSatoriRepository would go here if implemented
    // For now we can use a dummy or implement the real one later
    App(initialRoute = initialRoute)
}
