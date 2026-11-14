package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongMapperTest {

    @Test
    fun `titulo vacio usa el nombre del archivo sin extension`() {
        assertEquals("pista 01", songFrom(1, " ", "A", "B", 1000, "pista 01.mp3").title)
        assertEquals("Agua de mar", songFrom(1, "Agua de mar", "A", "B", 1000, "x.mp3").title)
    }

    @Test
    fun `artista y album desconocidos quedan nulos`() {
        val s = songFrom(1, "T", "<unknown>", "", 1000, "x.mp3")
        assertNull(s.artist)
        assertNull(s.album)
        assertEquals("Ana Vela", songFrom(1, "T", "Ana Vela", null, 1000, "x.mp3").artist)
    }

    @Test
    fun `duracion negativa queda en cero`() {
        assertEquals(0L, songFrom(1, "T", null, null, -5, "x.mp3").durationMs)
    }
}
