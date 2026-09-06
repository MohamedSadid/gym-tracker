package com.bool.gymtracker.domain

data class RestTimerState(
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val running: Boolean,
    val finished: Boolean,
) {
    fun tick(): RestTimerState {
        if (!running || remainingSeconds <= 0) return this
        val next = remainingSeconds - 1
        return if (next <= 0) copy(remainingSeconds = 0, running = false, finished = true)
        else copy(remainingSeconds = next)
    }

    fun pause() = copy(running = false)

    fun resume(): RestTimerState =
        if (remainingSeconds > 0) copy(running = true, finished = false) else this

    fun skip() = copy(remainingSeconds = 0, running = false, finished = false)

    fun addSeconds(seconds: Int): RestTimerState {
        val next = (remainingSeconds + seconds).coerceAtLeast(0)
        return copy(remainingSeconds = next, finished = false, running = running && next > 0)
    }

    companion object {
        fun start(seconds: Int) = RestTimerState(
            remainingSeconds = seconds,
            totalSeconds = seconds,
            running = true,
            finished = false,
        )
    }
}
