package com.rmstu.brsdd.util

/** Tracks recent frame timestamps to report a smoothed FPS value. */
class FpsCounter(private val windowSize: Int = 15) {
    private val timestamps = ArrayDeque<Long>()

    fun tick(): Float {
        val now = System.currentTimeMillis()
        timestamps.addLast(now)
        while (timestamps.size > windowSize) timestamps.removeFirst()
        if (timestamps.size < 2) return 0f
        val elapsedSeconds = (timestamps.last() - timestamps.first()) / 1000f
        return if (elapsedSeconds <= 0f) 0f else (timestamps.size - 1) / elapsedSeconds
    }
}
