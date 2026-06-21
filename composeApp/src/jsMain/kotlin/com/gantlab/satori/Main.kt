package com.gantlab.satori

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.gantlab.satori.db.DriverFactory
import com.gantlab.satori.di.initKoin
import com.gantlab.satori.notifications.DummyNotificationManager
import com.gantlab.satori.notifications.NotificationManager
import org.jetbrains.compose.resources.configureWebResources
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    
    initKoin {
        modules(module {
            single { DriverFactory() }
            single<NotificationManager> { DummyNotificationManager() }
        })
    }

    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        App()
    }
}
