package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeAgoTest {

    private val ahora = 1_800_000_000_000L
    private fun hace(ms: Long) = timeAgo(ahora, ahora - ms)

    @Test
    fun `menos de un minuto o en el futuro es un momento`() {
        assertEquals(TimeAgo.Moment, hace(0))
        assertEquals(TimeAgo.Moment, hace(59_999))
        assertEquals(TimeAgo.Moment, hace(-5 * MIN))
    }

    @Test
    fun `cortes de minuto hora y dia`() {
        assertEquals(TimeAgo.Minutes(1), hace(MIN))
        assertEquals(TimeAgo.Minutes(59), hace(60 * MIN - 1))
        assertEquals(TimeAgo.Hours(1), hace(60 * MIN))
        assertEquals(TimeAgo.Hours(23), hace(24 * 60 * MIN - 1))
        assertEquals(TimeAgo.Days(1), hace(24 * 60 * MIN))
        assertEquals(TimeAgo.Days(2), hace(2 * 24 * 60 * MIN + 5 * MIN))
    }

    private companion object {
        const val MIN = 60_000L
    }
}
