package com.gantlab.satori

import android.util.Log

class AndroidAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) {
        Log.d("Analytics", "Event: $name, Params: $params")
        // Tu w przyszłości można dodać: 
        // Firebase.analytics.logEvent(name) { ... }
    }
}

actual fun getAnalytics(): Analytics = AndroidAnalytics()
