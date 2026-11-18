package com.luis.rola.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PlaylistDao {
    /** La más nueva primero; Favoritas no es una fila (es automática). */
    @Query("SELECT * FROM playlists ORDER BY created_at DESC, id DESC")
    abstract fun playlists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlist_songs ORDER BY playlist_id, position")
    abstract fun allSongs(): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    abstract fun playlist(id: Long): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlist_songs WHERE playlist_id = :id ORDER BY position")
    abstract fun songs(id: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlist_songs WHERE playlist_id = :id AND media_id = :mediaId")
    abstract suspend fun row(id: Long, mediaId: Long): PlaylistSongEntity?

    @Insert
    abstract suspend fun create(playlist: PlaylistEntity): Long

    /** Devuelve -1 si la canción ya estaba en la lista. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(song: PlaylistSongEntity): Long

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_songs WHERE playlist_id = :id")
    protected abstract suspend fun maxPosition(id: Long): Long

    @Query("UPDATE playlists SET updated_at = :now WHERE id = :id")
    protected abstract suspend fun touch(id: Long, now: Long)

    /** Agrega al final en el orden dado, sin duplicar. Devuelve cuántas entraron. */
    @Transaction
    open suspend fun append(id: Long, mediaIds: List<Long>, now: Long): Int {
        val start = maxPosition(id) + 1
        var added = 0
        for (mediaId in mediaIds) {
            if (insert(PlaylistSongEntity(id, mediaId, start + added, now)) != -1L) added++
        }
        if (added > 0) touch(id, now)
        return added
    }

    @Query("DELETE FROM playlist_songs WHERE playlist_id = :id AND media_id = :mediaId")
    protected abstract suspend fun delete(id: Long, mediaId: Long)

    @Transaction
    open suspend fun remove(id: Long, mediaId: Long, now: Long) {
        delete(id, mediaId)
        touch(id, now)
    }

    /** Sin interfaz por ahora (fuera de alcance); las pruebas la usan para la cascada. */
    @Query("DELETE FROM playlists WHERE id = :id")
    abstract suspend fun deletePlaylist(id: Long)

    @Query("DELETE FROM favorites WHERE media_id = :mediaId")
    protected abstract suspend fun forgetFavorite(mediaId: Long)

    @Query("DELETE FROM playlist_songs WHERE media_id = :mediaId")
    protected abstract suspend fun forgetInPlaylists(mediaId: Long)

    /** Tras un borrado del dispositivo confirmado (DEC-08): fuera de Favoritas y de todas las listas. */
    @Transaction
    open suspend fun forgetSong(mediaId: Long) {
        forgetFavorite(mediaId)
        forgetInPlaylists(mediaId)
    }
}
