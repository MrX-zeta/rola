package com.luis.rola.core.data

import com.luis.rola.core.database.memoryDb
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryRepositoryTest {

    private val db = memoryDb()
    private val a = Song(1, "Agua", null, null, 60_000)
    private val b = Song(2, "Brillo", null, null, 120_000)
    private val songs = MutableStateFlow(listOf(a, b))
    private var now = 0L
    private val repo = LibraryRepository(db, songs) { ++now }

    @After
    fun cerrar() = db.close()

    @Test
    fun favoritasLaUltimaMarcadaArriba() = runBlocking {
        repo.setFavorite(a.id, true)
        repo.setFavorite(b.id, true)
        assertEquals(listOf(b, a), repo.favorites.first())
        repo.setFavorite(b.id, false)
        assertEquals(listOf(a), repo.favorites.first())
    }

    @Test
    fun unaCancionQueDesapareceSeOcultaYVuelveSinPerderSuFila() = runBlocking {
        repo.setFavorite(a.id, true)
        val lista = repo.createPlaylist("Viaje")
        repo.addToPlaylist(lista, listOf(a.id, b.id))
        songs.value = listOf(b)
        assertEquals(emptyList<Song>(), repo.favorites.first())
        assertEquals(1, repo.playlists.first().single().count)
        songs.value = listOf(a, b)
        assertEquals(listOf(a), repo.favorites.first())
        assertEquals(listOf(a, b), repo.playlist(lista).first()?.songs)
    }

    @Test
    fun quitarDeFavoritasYDeshacerVuelveASuLugar() = runBlocking {
        repo.setFavorite(a.id, true)
        repo.setFavorite(b.id, true)
        val deshacer = repo.remove(FAVORITAS_ID, a.id)
        assertEquals(listOf(b), repo.favorites.first())
        deshacer()
        assertEquals(listOf(b, a), repo.favorites.first())
    }
}
