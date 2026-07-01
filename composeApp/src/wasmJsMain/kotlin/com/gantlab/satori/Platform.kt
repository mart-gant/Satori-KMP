package com.gantlab.satori

interface Platform {
    val name: String
    fun shareText(text: String)
    fun setLanguage(languageCode: String)
}

class WasmPlatform : Platform {
    override val name: String = "Web"
    override fun shareText(text: String) { println("Sharing: $text") }
    override fun setLanguage(languageCode: String) { println("Setting lang: $languageCode") }
}

actual fun getPlatform(): Platform = WasmPlatform()

interface Analytics {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
}

actual fun getAnalytics(): Analytics = object : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) { println("Analytics: $name") }
}
