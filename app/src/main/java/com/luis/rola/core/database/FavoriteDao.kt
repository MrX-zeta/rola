package com.luis.rola.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    /** La más reciente primero (DEC-29). */
    @Query("SELECT media_id FROM favorites ORDER BY added_at DESC")
    fun ids(): Flow<List<Long>>

    @Query("SELECT * FROM favorites WHERE media_id = :mediaId")
    suspend fun get(mediaId: Long): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE media_id = :mediaId")
    suspend fun remove(mediaId: Long)
}
