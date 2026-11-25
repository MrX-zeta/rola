package com.luis.rola.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface QueueDao {
    @Query("SELECT * FROM playback_queue ORDER BY position")
    suspend fun rows(): List<QueueEntity>

    @Query("DELETE FROM playback_queue")
    suspend fun clear()

    @Insert
    suspend fun insertAll(rows: List<QueueEntity>)

    @Transaction
    suspend fun replace(rows: List<QueueEntity>) {
        clear()
        insertAll(rows)
    }
}
