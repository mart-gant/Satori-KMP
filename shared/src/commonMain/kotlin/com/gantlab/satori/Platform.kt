package com.gantlab.satori

interface Platform {
    val name: String
    fun shareText(text: String)
}

expect fun getPlatform(): Platform
