package com.gantlab.satori

class JvmPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
    override fun shareText(text: String) {
        println("Sharing on JVM: $text")
    }
}

actual fun getPlatform(): Platform = JvmPlatform()
