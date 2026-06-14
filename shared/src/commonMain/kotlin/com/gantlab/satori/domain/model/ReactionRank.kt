package com.gantlab.satori.domain.model

enum class ReactionRank(val label: String, val thresholdMs: Long) {
    NINJA("Ninja", 0),
    GEPARD("Gepard", 200),
    SOKOL("Sokół", 250),
    HUMAN("Człowiek", 300),
    LENIWIEC("Leniwiec", 400);

    companion object {
        fun fromTime(timeMs: Long?): ReactionRank {
            if (timeMs == null) return HUMAN
            return values()
                .filter { timeMs >= it.thresholdMs }
                .minByOrNull { timeMs - it.thresholdMs } ?: LENIWIEC
        }
    }
}
