package com.luis.rola.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class, PlaylistEntity::class, PlaylistSongEntity::class,
        PlayHistoryEntity::class, QueueEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)], // solo agrega tablas
)
abstract class RolaDatabase : RoomDatabase() {
    abstract fun favorites(): FavoriteDao
    abstract fun playlists(): PlaylistDao
    abstract fun history(): HistoryDao
    abstract fun queue(): QueueDao
}
