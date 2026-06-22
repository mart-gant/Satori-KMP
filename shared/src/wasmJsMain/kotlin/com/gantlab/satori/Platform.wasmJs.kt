package com.gantlab.satori

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"

    override fun shareText(text: String) {
        // Implementation for Web share or copy to clipboard
    }

    override fun setLanguage(lang: String) {
        // Runtime locale switching is handled by platform-specific UI layers.
    }
}

actual fun getPlatform(): Platform = WasmPlatform()
