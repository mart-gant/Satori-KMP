package com.gantlab.satori

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform