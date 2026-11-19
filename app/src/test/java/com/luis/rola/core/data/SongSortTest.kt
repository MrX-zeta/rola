package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SongSortTest {

    private val canciones = listOf(
        Song(1, "Brillo", "Los Faros", "Faros", 0),
        Song(2, "Agua", "Ana Vela", "Ventanas", 0),
        Song(3, "Calle", "Ana Vela", "Agua", 0),
        Song(4, "Eco", null, null, 0),
    )

    private fun titulos(sort: SongSort) = canciones.sortedWith(sort.comparator).map { it.title }

    @Test
    fun `por titulo en las dos direcciones`() {
        assertEquals(listOf("Agua", "Brillo", "Calle", "Eco"), titulos(SongSort(SortBy.TITLE)))
        assertEquals(listOf("Eco", "Calle", "Brillo", "Agua"), titulos(SongSort(SortBy.TITLE, ascending = false)))
    }

    @Test
    fun `por artista con empate por titulo y desconocido al final`() {
        assertEquals(listOf("Agua", "Calle", "Brillo", "Eco"), titulos(SongSort(SortBy.ARTIST)))
        assertEquals(listOf("Eco", "Brillo", "Calle", "Agua"), titulos(SongSort(SortBy.ARTIST, ascending = false)))
    }

    @Test
    fun `por album en las dos direcciones`() {
        assertEquals(listOf("Calle", "Brillo", "Agua", "Eco"), titulos(SongSort(SortBy.ALBUM)))
        assertEquals(listOf("Eco", "Agua", "Brillo", "Calle"), titulos(SongSort(SortBy.ALBUM, ascending = false)))
    }

    @Test
    fun `la seccion sale del criterio`() {
        val s = canciones[0]
        assertEquals("B", SongSort(SortBy.TITLE).sectionOf(s))
        assertEquals("L", SongSort(SortBy.ARTIST).sectionOf(s))
        assertEquals("F", SongSort(SortBy.ALBUM).sectionOf(s))
        assertEquals("#", SongSort(SortBy.ARTIST).sectionOf(canciones[3]))
    }

    @Test
    fun `guardar y leer el orden y valores invalidos`() {
        SortBy.entries.forEach { by ->
            listOf(true, false).forEach { asc -> SongSort(by, asc).let { assertEquals(it, SongSort.parse(it.key())) } }
        }
        assertEquals(SongSort(), SongSort.parse(null))
        assertEquals(SongSort(), SongSort.parse("GENERO_ASC"))
        assertEquals(SongSort(), SongSort.parse("ARTIST_X"))
    }
}
