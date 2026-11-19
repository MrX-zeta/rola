package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryGroupingTest {

    private fun cancion(
        id: Long, titulo: String, album: String?, albumId: Long, artista: String? = "Ana Vela",
        artistaAlbum: String? = null, pista: Int = 0, anio: Int = 0,
    ) = Song(id, titulo, artista, album, 60_000, albumId, artistaAlbum, pista, anio)

    @Test
    fun `un album por identificador aunque se llamen igual`() {
        val albums = LibraryGrouping.albums(
            listOf(
                cancion(1, "Uno", "Greatest Hits", 10, artista = "Club Sol"),
                cancion(2, "Dos", "Greatest Hits", 20, artista = "Los Faros"),
            ),
        )
        assertEquals(listOf(10L, 20L).toSet(), albums.map { it.id }.toSet())
    }

    @Test
    fun `pistas en orden con disco y las que no tienen numero al final por titulo`() {
        val album = LibraryGrouping.albums(
            listOf(
                cancion(1, "Zeta", "Mar", 1, pista = 0),
                cancion(2, "Segundo disco", "Mar", 1, pista = 2001),
                cancion(3, "Tres", "Mar", 1, pista = 3),
                cancion(4, "Alfa", "Mar", 1, pista = 0),
                cancion(5, "Uno", "Mar", 1, pista = 1),
            ),
        ).single()
        assertEquals(listOf("Uno", "Tres", "Segundo disco", "Alfa", "Zeta"), album.songs.map { it.title })
    }

    @Test
    fun `artista del album nombre y anio`() {
        val album = LibraryGrouping.albums(
            listOf(
                cancion(1, "A", "Mar", 1, artista = "Invitada", pista = 1, anio = 2020),
                cancion(2, "B", "Mar", 1, artista = "Ana Vela", artistaAlbum = "Marea Alta", pista = 2, anio = 2023),
            ),
        ).single()
        assertEquals("Mar", album.name)
        assertEquals("Marea Alta", album.artist)
        assertEquals(2023, album.year)
        // Sin artista del álbum: el de la primera pista.
        assertEquals("Invitada", LibraryGrouping.albums(listOf(cancion(1, "A", "X", 2, artista = "Invitada", pista = 1))).single().artist)
    }

    @Test
    fun `albumes y artistas por nombre con DEC-05`() {
        val songs = listOf(
            cancion(1, "a", "Ñandú", 1, artista = "Ñu"),
            cancion(2, "b", "Ópalo", 2, artista = "Óscar"),
            cancion(3, "c", "Nube", 3, artista = "Nadia"),
        )
        assertEquals(listOf("Nube", "Ñandú", "Ópalo"), LibraryGrouping.albums(songs).map { it.name })
        assertEquals(listOf("Nadia", "Ñu", "Óscar"), LibraryGrouping.artists(songs).map { it.name })
    }

    @Test
    fun `artista desconocido agrupa los vacios en una sola entrada al final`() {
        val artistas = LibraryGrouping.artists(
            listOf(
                cancion(1, "Uno", "X", 1, artista = null),
                cancion(2, "Dos", "Y", 2, artista = null),
                cancion(3, "Tres", "Z", 3, artista = "Club Sol"),
            ),
        )
        assertEquals(2, artistas.size)
        assertNull(artistas.last().name)
        assertEquals(2, artistas.last().songs.size)
    }

    @Test
    fun `artista usa el artista del album y sus albumes del mas reciente`() {
        val artista = LibraryGrouping.artists(
            listOf(
                cancion(1, "Viejo", "Primeras", 1, anio = 2018, pista = 1),
                cancion(2, "Nuevo 2", "Ventanas", 2, anio = 2022, pista = 2),
                cancion(3, "Nuevo 1", "Ventanas", 2, anio = 2022, pista = 1),
                cancion(4, "Invitada", "Ajeno", 3, artista = "Otra", artistaAlbum = "Ana Vela", anio = 2020),
            ),
        ).single()
        assertEquals("Ana Vela", artista.name)
        assertEquals(listOf("Ventanas", "Ajeno", "Primeras"), artista.albums.map { it.name })
        assertEquals(listOf("Nuevo 1", "Nuevo 2", "Invitada", "Viejo"), artista.songs.map { it.title })
        assertEquals(3L, artista.cover?.id)
    }
}
