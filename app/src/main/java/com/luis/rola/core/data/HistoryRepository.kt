package com.luis.rola.core.data

import android.database.sqlite.SQLiteException
import com.luis.rola.core.database.HistoryDao
import com.luis.rola.core.database.PlayHistoryEntity
import com.luis.rola.core.database.PlayStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class MostPlayed(val song: Song, val plays: Int)

/** [recent]: hasta 10 canciones distintas, la más reciente primero. [mostPlayed] nulo sin historial. */
data class Listened(val recent: List<Song>, val mostPlayed: MostPlayed?)

/** Historial (TBL-HISTORY) cruzado con las canciones que existen (FR-011, FR-012). */
class HistoryRepository(
    private val dao: HistoryDao,
    songs: Flow<List<Song>>,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    val listened: Flow<Listened> = combine(dao.stats(), songs, ::listened)

    suspend fun record(mediaId: Long) {
        // Disco lleno: se pierde esta escucha, no la música.
        try {
            dao.add(PlayHistoryEntity(mediaId = mediaId, playedAt = clock()))
        } catch (_: SQLiteException) {
        }
    }
}

internal fun listened(stats: List<PlayStats>, songs: List<Song>): Listened {
    val byId = songs.associateBy { it.id }
    return Listened(
        recent = existing(stats.sortedByDescending { it.lastPlayedAt }.map { it.mediaId }, byId).take(10),
        mostPlayed = stats.sortedWith(compareByDescending<PlayStats> { it.plays }.thenByDescending { it.lastPlayedAt })
            .firstNotNullOfOrNull { s -> byId[s.mediaId]?.let { MostPlayed(it, s.plays) } },
    )
}
