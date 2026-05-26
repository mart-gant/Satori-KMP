package com.gantlab.satori

import platform.UIKit.UIDevice
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override fun shareText(text: String) {
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController
        
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null
        )
        
        rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
    }

    override fun setLanguage(lang: String) {
        // On iOS, changing language at runtime without app restart is tricky
        // with standard localization. For Compose Resources, it might require
        // a custom implementation of the string provider.
        println("iOS: Language change requested to $lang (Requires app restart or custom provider)")
    }
}

actual fun getPlatform(): Platform = IOSPlatform()
