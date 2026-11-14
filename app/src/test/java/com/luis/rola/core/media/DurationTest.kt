package com.luis.rola.core.media

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationTest {
    @Test
    fun `formatea minutos y horas`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:00", formatDuration(-1))
        assertEquals("1:05", formatDuration(65_000))
        assertEquals("1:01:05", formatDuration(3_665_000))
    }
}
