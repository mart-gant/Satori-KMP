package com.gantlab.satori

class WebAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) {
        println("Analytics Event: $name, Params: $params")
    }
}

actual fun getAnalytics(): Analytics = WebAnalytics()
