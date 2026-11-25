package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {

    private fun tramo(texto: String, consulta: String) =
        SearchableText(texto).find(TitleOrder.normalizar(consulta))?.let { texto.substring(it) }

    @Test
    fun `el tramo es del texto original, con sus acentos y mayusculas`() {
        assertEquals("mar", tramo("Agua de mar", "MAR"))
        assertEquals("Canción", tramo("Canción de cuna", "cancion"))
        assertEquals("ción", tramo("Canción", "cion"))
        assertEquals("ñandú", tramo("El ñandú", "ÑANDU"))
        assertNull(tramo("El ñandú", "nandu"))
        assertNull(tramo("Brillo", "mar"))
    }

    @Test
    fun `un acento suelto se resalta con su letra`() {
        val suelto = "Cafe\u0301 solo" // «é» en dos caracteres
        assertEquals("Cafe\u0301", tramo(suelto, "cafe"))
        assertEquals("Cafe\u0301", tramo("Cafe\u0301", "café"))
        assertEquals("solo", tramo(suelto, "solo"))
    }

    private fun songs(vararg titulos: String) = titulos.mapIndexed { i, t -> Song(i + 1L, t, "Marea Alta", "Mar adentro", 0) }

    private fun indice(songs: List<Song> = emptyList(), listas: List<String> = emptyList()): LibraryIndex {
        val albums = LibraryGrouping.albums(songs)
        return LibraryIndex(songs, albums, LibraryGrouping.artists(songs, albums), listas.map { PlaylistSummary(it.hashCode().toLong(), it, 0, emptyList()) })
    }

    @Test
    fun `primero lo que empieza y luego lo que contiene, en su orden, maximo 5`() {
        val r = indice(songs("Agua de mar", "Amar", "Mar adentro", "Mareo", "Altamar", "Marzo", "Zumar")).search(" mar ")!!
        assertEquals("mar", r.query)
        assertEquals(listOf("Mar adentro", "Mareo", "Marzo", "Agua de mar", "Amar"), r.songs.map { it.item.title })
    }

    @Test
    fun `compara por el nombre de cada elemento`() {
        val r = indice(songs("Sal")).search("mar")!!
        assertTrue(r.songs.isEmpty()) // su artista y su álbum coinciden, su título no
        assertEquals(listOf("Mar adentro"), r.albums.map { it.item.name })
        assertEquals(listOf("Marea Alta"), r.artists.map { it.item.name })
    }

    @Test
    fun `las listas con Favoritas se buscan por su nombre`() {
        val r = indice(listas = listOf("Favoritas", "Para el mar")).search("fav")!!
        assertEquals(listOf("Favoritas"), r.playlists.map { it.item.name })
        assertEquals(0..2, r.playlists.single().match)
    }

    @Test
    fun `los desconocidos no salen y sin coincidencias queda vacio`() {
        val r = indice(listOf(Song(1, "Sal", null, null, 0))).search("desconocido")!!
        assertTrue(r.isEmpty)
    }

    @Test
    fun `solo espacios es no buscar nada`() = assertNull(indice(songs("Mar")).search("   "))
}
