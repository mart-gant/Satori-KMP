package com.gantlab.satori

interface Platform {
    val name: String
    fun shareText(text: String)
    fun setLanguage(lang: String)
}

expect fun getPlatform(): Platform
