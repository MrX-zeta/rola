package com.luis.rola.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** TBL-FAVORITES. [mediaId] = `MediaStore.Audio.Media._ID` (`Song.id`). */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey @ColumnInfo(name = "media_id") val mediaId: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

/** TBL-PLAYLISTS. */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

/** TBL-PLAYLIST-SONGS. Una canción va una sola vez por lista; se va con su lista. */
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlist_id", "media_id"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("media_id")],
)
data class PlaylistSongEntity(
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    @ColumnInfo(name = "media_id") val mediaId: Long,
    val position: Long,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)

/** TBL-HISTORY: una fila por escucha que contó. */
@Entity(tableName = "play_history", indices = [Index("media_id", "played_at")])
data class PlayHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "media_id") val mediaId: Long,
    @ColumnInfo(name = "played_at") val playedAt: Long,
)

/** TBL-QUEUE: la cola guardada (DEC-31). [position]: lugar en la lista de Media3; [playOrder]: lugar en que suena. */
@Entity(tableName = "playback_queue")
data class QueueEntity(
    @PrimaryKey val position: Int,
    @ColumnInfo(name = "media_id") val mediaId: Long,
    @ColumnInfo(name = "play_order") val playOrder: Int,
)
