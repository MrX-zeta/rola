package com.luis.rola.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlayCounterTest {

    private var t = 0L
    private val c = PlayCounter { t }

    private fun suena(ms: Long) {
        c.playing(true)
        t += ms
    }

    @Test
    fun `cuenta a los 30 s sonando y una sola vez`() {
        c.start(1)
        suena(29_999)
        assertNull(c.due())
        assertEquals(1L, c.remainingMs)
        t += 1
        assertEquals(1L, c.due())
        t += 60_000
        assertNull(c.due())
        assertNull(c.remainingMs)
        assertNull(c.ended(90_000))
    }

    @Test
    fun `el tiempo en pausa no suma y los tramos si`() {
        c.start(1)
        suena(20_000)
        c.playing(false)
        assertNull(c.remainingMs)
        t += 600_000
        assertNull(c.due())
        suena(10_000)
        assertEquals(1L, c.due())
    }

    @Test
    fun `una corta cuenta si termina y no si se salta`() {
        c.start(1)
        suena(8_000)
        assertEquals(1L, c.ended(8_000))
        c.start(2)
        t += 5_000
        c.start(3) // se saltó la 2
        t += 8_000
        assertEquals(3L, c.ended(8_000))
    }

    @Test
    fun `una larga adelantada hasta el final no cuenta`() {
        c.start(1)
        suena(3_000)
        assertNull(c.ended(200_000))
    }

    @Test
    fun `una corta adelantada sin sonar nada no cuenta`() {
        c.start(1)
        assertNull(c.ended(8_000))
    }

    @Test
    fun `repetir una cuenta en cada vuelta`() {
        c.start(1)
        suena(8_000)
        assertEquals(1L, c.ended(8_000))
        c.start(1)
        t += 8_000
        assertEquals(1L, c.ended(8_000))
    }

    @Test
    fun `pausada al final por el temporizador no cuenta dos veces`() {
        c.start(1)
        suena(8_000)
        assertEquals(1L, c.ended(8_000))
        c.playing(false)
        c.playing(true) // sigue: la transición vuelve a avisar del final
        assertNull(c.ended(8_000))
    }

    @Test
    fun `sin cancion no cuenta`() {
        c.start(null)
        suena(40_000)
        assertNull(c.due())
    }
}
