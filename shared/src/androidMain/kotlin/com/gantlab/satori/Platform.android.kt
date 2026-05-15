package com.gantlab.satori

import android.content.Context
import android.content.Intent
import android.os.Build

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

// In a real app with Koin, this context would be injected.
// For now, we'll need to handle how this actual fun is called if it needs context.
// Alternatively, we can use a global context or change the architecture.
lateinit var appContext: Context

actual fun getPlatform(): Platform = AndroidPlatform(appContext)
