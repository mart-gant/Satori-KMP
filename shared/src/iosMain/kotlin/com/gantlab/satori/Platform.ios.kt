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
}

actual fun getPlatform(): Platform = IOSPlatform()
