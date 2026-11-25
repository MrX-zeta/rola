package com.luis.rola.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/** Base en memoria para las pruebas de Room. */
fun memoryDb(): RolaDatabase =
    Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), RolaDatabase::class.java).build()

class RolaDatabaseTest {

    private val db = memoryDb()
    private val favorites = db.favorites()
    private val playlists = db.playlists()

    @After
    fun cerrar() = db.close()

    private suspend fun ids(list: Long) = playlists.songs(list).first().map { it.mediaId }

    @Test
    fun favoritasLaMasRecienteArribaSinRepetir() = runBlocking {
        favorites.add(FavoriteEntity(10, addedAt = 1))
        favorites.add(FavoriteEntity(20, addedAt = 2))
        favorites.add(FavoriteEntity(10, addedAt = 3)) // ya estaba: no cambia nada
        assertEquals(listOf(20L, 10L), favorites.ids().first())
        favorites.remove(20)
        assertEquals(listOf(10L), favorites.ids().first())
    }

    @Test
    fun agregarVaAlFinalEnOrdenYSinDuplicar() = runBlocking {
        val id = playlists.create(PlaylistEntity(name = "Viaje", createdAt = 1, updatedAt = 1))
        assertEquals(2, playlists.append(id, listOf(1, 2), now = 5))
        assertEquals(1, playlists.append(id, listOf(2, 3), now = 6))
        assertEquals(listOf(1L, 2L, 3L), ids(id))
        assertEquals(6L, playlists.playlist(id).first()?.updatedAt)
    }

    @Test
    fun quitarYDeshacerConservaElLugar() = runBlocking {
        val id = playlists.create(PlaylistEntity(name = "Viaje", createdAt = 1, updatedAt = 1))
        playlists.append(id, listOf(1, 2, 3), now = 5)
        val fila = playlists.row(id, 2)!!
        playlists.remove(id, 2, now = 6)
        assertEquals(listOf(1L, 3L), ids(id))
        playlists.insert(fila)
        assertEquals(listOf(1L, 2L, 3L), ids(id))
    }

    @Test
    fun borrarLaListaBorraSusCancionesEnCascada() = runBlocking {
        val id = playlists.create(PlaylistEntity(name = "Viaje", createdAt = 1, updatedAt = 1))
        playlists.append(id, listOf(1, 2), now = 5)
        playlists.deletePlaylist(id)
        assertEquals(emptyList<PlaylistSongEntity>(), playlists.allSongs().first())
    }

    @Test
    fun olvidarUnaCancionLaQuitaDeFavoritasYDeTodasLasListas() = runBlocking {
        val a = playlists.create(PlaylistEntity(name = "A", createdAt = 1, updatedAt = 1))
        val b = playlists.create(PlaylistEntity(name = "B", createdAt = 2, updatedAt = 2))
        playlists.append(a, listOf(7, 8), now = 3)
        playlists.append(b, listOf(7), now = 3)
        favorites.add(FavoriteEntity(7, addedAt = 3))
        db.history().add(PlayHistoryEntity(mediaId = 7, playedAt = 4))
        db.history().add(PlayHistoryEntity(mediaId = 8, playedAt = 5))
        playlists.forgetSong(7)
        assertEquals(listOf(PlayStats(8, 1, 5)), db.history().stats().first())
        assertEquals(listOf(8L), ids(a))
        assertEquals(emptyList<Long>(), ids(b))
        assertEquals(emptyList<Long>(), favorites.ids().first())
    }

    @Test
    fun historialAgrupaCuantasYCuandoPorUltimaVez() = runBlocking {
        listOf(1L to 10L, 2L to 20L, 1L to 30L).forEach { (id, t) -> db.history().add(PlayHistoryEntity(mediaId = id, playedAt = t)) }
        assertEquals(setOf(PlayStats(1, 2, 30), PlayStats(2, 1, 20)), db.history().stats().first().toSet())
    }

    @Test
    fun laColaSeReemplazaCompletaYAdmiteRepetidas() = runBlocking {
        val cola = listOf(QueueEntity(0, 5, 1), QueueEntity(1, 5, 0))
        db.queue().replace(listOf(QueueEntity(0, 9, 0), QueueEntity(1, 8, 1), QueueEntity(2, 7, 2)))
        db.queue().replace(cola)
        assertEquals(cola, db.queue().rows())
        db.queue().replace(emptyList())
        assertEquals(emptyList<QueueEntity>(), db.queue().rows())
    }

    @Test
    fun listasLaMasNuevaArriba() = runBlocking {
        playlists.create(PlaylistEntity(name = "Vieja", createdAt = 1, updatedAt = 1))
        playlists.create(PlaylistEntity(name = "Nueva", createdAt = 2, updatedAt = 2))
        assertEquals(listOf("Nueva", "Vieja"), playlists.playlists().first().map { it.name })
    }

    @Test
    fun renombrarYBorrarConDeshacerRestauraTodo() = runBlocking {
        val id = playlists.create(PlaylistEntity(name = "Viaje", createdAt = 1, updatedAt = 1))
        playlists.append(id, listOf(3, 1, 2), now = 5)
        playlists.rename(id, "Noche", now = 7)
        val antes = playlists.get(id)!!
        assertEquals("Noche", antes.name)
        assertEquals(7L, antes.updatedAt)

        val filas = playlists.rows(id)
        playlists.deletePlaylist(id)
        assertEquals(null, playlists.get(id))
        playlists.restore(antes, filas)
        assertEquals(antes, playlists.get(id))
        assertEquals(listOf(3L, 1L, 2L), ids(id))
    }
}
