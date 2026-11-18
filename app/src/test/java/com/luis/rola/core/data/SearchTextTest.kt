package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTextTest {

    private val cancion = Song(1, "Canción de cuna", "Ána Vela", "Ñandú", 0)

    @Test
    fun sinAcentosNiMayusculas() {
        assertTrue(SearchText.matches(cancion, "cancion"))
        assertTrue(SearchText.matches(cancion, "  CANCIÓN "))
    }

    @Test
    fun buscaPorArtistaYAlbum() {
        assertTrue(SearchText.matches(cancion, "ana"))
        assertTrue(SearchText.matches(cancion, "ñandu"))
        assertFalse(SearchText.matches(cancion, "faros"))
    }

    @Test
    fun laEñeNoEsEne() {
        assertFalse(SearchText.matches(cancion, "nandu"))
    }

    @Test
    fun consultaVaciaLasMuestraTodas() {
        assertTrue(SearchText.matches(Song(2, "X", null, null, 0), "   "))
    }

    @Test
    fun nombreDeLista() {
        assertNull(PlaylistName.clean("   "))
        assertEquals("Viaje", PlaylistName.clean("  Viaje "))
        assertEquals(PlaylistName.MAX, PlaylistName.clean("a".repeat(61))!!.length)
    }

    @Test
    fun resumenOmiteInexistentesYTomaCuatroPortadas() {
        val canciones = (1L..6L).associateWith { Song(it, "c$it", null, null, 1_000) }
        val presentes = existing(listOf(9, 1, 2, 3, 4, 5), canciones)
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), presentes.map { it.id })
        val r = summary(7, "Viaje", presentes)
        assertEquals(5, r.count)
        assertEquals(listOf(1L, 2L, 3L, 4L), r.covers.map { it.id })
        assertEquals(5_000L, PlaylistDetail(7, "Viaje", presentes).totalMs)
    }
}
