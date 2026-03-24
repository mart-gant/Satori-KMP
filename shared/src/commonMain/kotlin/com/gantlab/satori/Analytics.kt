package com.gantlab.satori

interface Analytics {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}

expect fun getAnalytics(): Analytics

object AnalyticsEvents {
    const val TEST_STARTED = "test_started"
    const val TEST_FINISHED = "test_finished"
    const val SHARE_CLICKED = "share_clicked"
    const val SCREEN_VIEW = "screen_view"
}
