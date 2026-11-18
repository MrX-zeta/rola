package com.luis.rola.core.data

import com.luis.rola.core.database.FavoriteEntity
import com.luis.rola.core.database.PlaylistEntity
import com.luis.rola.core.database.RolaDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Favoritas en la navegación y en el menú: no es una fila de `playlists` (DEC-29). */
const val FAVORITAS_ID = -1L

/** [name] vacío en Favoritas: la interfaz pone el suyo. [covers]: hasta 4 para el mosaico. */
data class PlaylistSummary(val id: Long, val name: String, val count: Int, val covers: List<Song>)

data class PlaylistDetail(val id: Long, val name: String, val songs: List<Song>) {
    val totalMs: Long get() = songs.sumOf { it.durationMs }
}

/**
 * Favoritas y listas (Room) cruzadas con las canciones del MediaStore: lo que ya no existe se omite
 * al mostrar pero su fila se conserva (FR-031); solo [forgetSong] la borra, tras un borrado propio.
 */
class LibraryRepository(
    db: RolaDatabase,
    songs: Flow<List<Song>>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val favoriteDao = db.favorites()
    private val playlistDao = db.playlists()
    private val byId = songs.map { list -> list.associateBy { it.id } }

    val favoriteIds: Flow<Set<Long>> = favoriteDao.ids().map { it.toSet() }

    val favorites: Flow<List<Song>> = combine(favoriteDao.ids(), byId, ::existing)

    val favoritesSummary: Flow<PlaylistSummary> = favorites.map { summary(FAVORITAS_ID, "", it) }

    val playlists: Flow<List<PlaylistSummary>> =
        combine(playlistDao.playlists(), playlistDao.allSongs(), byId) { lists, rows, songsById ->
            val byList = rows.groupBy({ it.playlistId }, { it.mediaId })
            lists.map { summary(it.id, it.name, existing(byList[it.id].orEmpty(), songsById)) }
        }

    /** `null` si la lista no existe. */
    fun playlist(id: Long): Flow<PlaylistDetail?> = if (id == FAVORITAS_ID) {
        favorites.map { PlaylistDetail(id, "", it) }
    } else {
        combine(playlistDao.playlist(id), playlistDao.songs(id), byId) { p, rows, songsById ->
            p?.let { PlaylistDetail(it.id, it.name, existing(rows.map { r -> r.mediaId }, songsById)) }
        }
    }

    suspend fun setFavorite(mediaId: Long, favorite: Boolean) {
        if (favorite) favoriteDao.add(FavoriteEntity(mediaId, clock())) else favoriteDao.remove(mediaId)
    }

    suspend fun createPlaylist(name: String): Long = clock().let { playlistDao.create(PlaylistEntity(name = name, createdAt = it, updatedAt = it)) }

    /** Devuelve cuántas entraron (las que ya estaban no se repiten). */
    suspend fun addToPlaylist(id: Long, mediaIds: List<Long>): Int = playlistDao.append(id, mediaIds, clock())

    /** Quita de una lista (o de Favoritas) y devuelve cómo deshacerlo, con su lugar y fecha de antes. */
    suspend fun remove(listId: Long, mediaId: Long): suspend () -> Unit {
        if (listId == FAVORITAS_ID) {
            val before = favoriteDao.get(mediaId)
            favoriteDao.remove(mediaId)
            return { before?.let { favoriteDao.add(it) } }
        }
        val before = playlistDao.row(listId, mediaId)
        playlistDao.remove(listId, mediaId, clock())
        return { before?.let { playlistDao.insert(it) } }
    }

    /** Tras un borrado del dispositivo confirmado (DEC-08). */
    suspend fun forgetSong(mediaId: Long) = playlistDao.forgetSong(mediaId)
}

internal fun existing(ids: List<Long>, songsById: Map<Long, Song>): List<Song> = ids.mapNotNull(songsById::get)

internal fun summary(id: Long, name: String, songs: List<Song>) = PlaylistSummary(id, name, songs.size, songs.take(4))
