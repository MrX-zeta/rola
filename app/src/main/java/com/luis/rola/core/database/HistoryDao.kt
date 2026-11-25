package com.luis.rola.core.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Cuántas veces y cuándo por última vez sonó una canción. */
data class PlayStats(
    @ColumnInfo(name = "media_id") val mediaId: Long,
    val plays: Int,
    @ColumnInfo(name = "last_played_at") val lastPlayedAt: Long,
)

@Dao
interface HistoryDao {
    @Insert
    suspend fun add(play: PlayHistoryEntity)

    @Query("SELECT media_id, COUNT(*) AS plays, MAX(played_at) AS last_played_at FROM play_history GROUP BY media_id")
    fun stats(): Flow<List<PlayStats>>
}
