package com.bool.gymtracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerStateTest {
    @Test
    fun countsDownThenFinishes() {
        var rest = RestTimerState.start(2)
        rest = rest.tick()
        assertEquals(1, rest.remainingSeconds)
        assertTrue(rest.running)
        rest = rest.tick()
        assertEquals(0, rest.remainingSeconds)
        assertFalse(rest.running)
        assertTrue(rest.finished)
    }

    @Test
    fun pauseAndResumeAndSkip() {
        var rest = RestTimerState.start(90).tick().pause()
        assertFalse(rest.running)
        assertEquals(89, rest.remainingSeconds)
        rest = rest.resume()
        assertTrue(rest.running)
        rest = rest.skip()
        assertEquals(0, rest.remainingSeconds)
        assertFalse(rest.finished)
    }

    @Test
    fun addFifteenSeconds() {
        val rest = RestTimerState.start(10).addSeconds(15)
        assertEquals(25, rest.remainingSeconds)
    }
}
