package com.gantlab.satori.utils

import kotlinx.datetime.Clock

object TimeUtils {
    fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()
}
