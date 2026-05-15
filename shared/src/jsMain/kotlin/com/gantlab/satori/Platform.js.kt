package com.gantlab.satori

class JsPlatform : Platform {
    override val name: String = "Web (JS)"
    override fun shareText(text: String) {
        // Implementation for Web share or copy to clipboard
    }
}

actual fun getPlatform(): Platform = JsPlatform()
