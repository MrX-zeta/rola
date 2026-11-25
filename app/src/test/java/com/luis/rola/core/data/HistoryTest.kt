package com.luis.rola.core.data

import com.luis.rola.core.database.PlayStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HistoryTest {

    private fun songs(vararg ids: Long) = ids.map { Song(it, "c$it", null, null, 0) }

    @Test
    fun `las ultimas 10 distintas, la mas reciente primero, y se completa si falta una`() {
        val stats = (1L..12L).map { PlayStats(it, plays = 1, lastPlayedAt = it * 100) }
        val ids = listened(stats, songs(*(1L..12L).toList().toLongArray())).recent.map { it.id }
        assertEquals((12L downTo 3L).toList(), ids)
        val sinLa11 = listened(stats, songs(*((1L..12L) - 11L).toLongArray())).recent.map { it.id }
        assertEquals(listOf(12L) + (10L downTo 2L), sinLa11)
    }

    @Test
    fun `la mas escuchada es la de mas veces y con empate la mas reciente`() {
        val stats = listOf(PlayStats(1, 5, 100), PlayStats(2, 5, 300), PlayStats(3, 4, 900))
        assertEquals(MostPlayed(songs(2).single(), 5), listened(stats, songs(1, 2, 3)).mostPlayed)
    }

    @Test
    fun `la mas escuchada que ya no existe se omite`() {
        val stats = listOf(PlayStats(1, 9, 100), PlayStats(2, 2, 50))
        assertEquals(MostPlayed(songs(2).single(), 2), listened(stats, songs(2)).mostPlayed)
        assertNull(listened(stats, songs(7)).mostPlayed)
        assertEquals(emptyList<Song>(), listened(stats, songs(7)).recent)
    }
}
