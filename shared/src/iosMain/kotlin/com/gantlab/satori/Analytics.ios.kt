package com.gantlab.satori

class IosAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) {
        println("Analytics Event: $name, Params: $params")
    }
}

actual fun getAnalytics(): Analytics = IosAnalytics()
