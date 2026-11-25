package com.luis.rola.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** v1 → v2 (FR-046): las sentencias son las de `schemas/1.json`. Room valida el esquema al abrir. */
class MigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @After
    fun borrar() {
        context.deleteDatabase(NOMBRE)
    }

    @Test
    fun conservaFavoritasYListasYAgregaTablasVacias() = runBlocking {
        context.deleteDatabase(NOMBRE)
        context.openOrCreateDatabase(NOMBRE, Context.MODE_PRIVATE, null).use { v1 ->
            V1.forEach(v1::execSQL)
            v1.execSQL("INSERT INTO favorites VALUES (10, 1)")
            v1.execSQL("INSERT INTO playlists VALUES (5, 'Viaje', 1, 2)")
            v1.execSQL("INSERT INTO playlist_songs VALUES (5, 10, 0, 3), (5, 20, 1, 4)")
            v1.version = 1
        }
        val db = Room.databaseBuilder(context, RolaDatabase::class.java, NOMBRE).build()
        try {
            assertEquals(listOf(10L), db.favorites().ids().first())
            assertEquals(listOf(PlaylistEntity(5, "Viaje", 1, 2)), db.playlists().playlists().first())
            assertEquals(listOf(10L, 20L), db.playlists().songs(5).first().map { it.mediaId })
            assertTrue(db.history().stats().first().isEmpty())
            assertTrue(db.queue().rows().isEmpty())
        } finally {
            db.close()
        }
    }

    private companion object {
        const val NOMBRE = "migracion_prueba.db"
        val V1 = listOf(
            "CREATE TABLE IF NOT EXISTS `favorites` (`media_id` INTEGER NOT NULL, `added_at` INTEGER NOT NULL, PRIMARY KEY(`media_id`))",
            "CREATE TABLE IF NOT EXISTS `playlists` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, " +
                "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)",
            "CREATE TABLE IF NOT EXISTS `playlist_songs` (`playlist_id` INTEGER NOT NULL, `media_id` INTEGER NOT NULL, " +
                "`position` INTEGER NOT NULL, `added_at` INTEGER NOT NULL, PRIMARY KEY(`playlist_id`, `media_id`), " +
                "FOREIGN KEY(`playlist_id`) REFERENCES `playlists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            "CREATE INDEX IF NOT EXISTS `index_playlist_songs_media_id` ON `playlist_songs` (`media_id`)",
        )
    }
}
