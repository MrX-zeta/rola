package com.luis.rola.core.data

import android.content.Context
import android.database.ContentObserver
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore.Audio.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

interface SongRepository {
    /** Canciones ordenadas por título (DEC-05); emite de nuevo cuando cambia el MediaStore (DEC-26). */
    fun songs(): Flow<List<Song>>

    /** «Escanear de nuevo» (DEC-26): el sistema recorre la carpeta Música y se vuelve a consultar. Devuelve cuántas hay. */
    suspend fun scan(): Int
}

class MediaStoreSongRepository(private val context: Context) : SongRepository {

    private val resolver = context.contentResolver
    private val consultar = MutableSharedFlow<Unit>()

    override fun songs(): Flow<List<Song>> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                trySend(Unit)
            }
        }
        resolver.registerContentObserver(Media.EXTERNAL_CONTENT_URI, true, observer)
        trySend(Unit)
        launch { consultar.collect { send(it) } }
        awaitClose { resolver.unregisterContentObserver(observer) }
    }.conflate().map { query() }.flowOn(Dispatchers.IO)

    override suspend fun scan(): Int {
        @Suppress("DEPRECATION") // la ruta de la carpeta es justo lo que pide el escáner
        val musica = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path
        withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine { c ->
                MediaScannerConnection.scanFile(context, arrayOf(musica), null) { _, _ -> if (c.isActive) c.resume(Unit) }
            }
        }
        consultar.emit(Unit)
        return withContext(Dispatchers.IO) { query().size }
    }

    private fun query(): List<Song> = try {
        resolver.query(Media.EXTERNAL_CONTENT_URI, COLUMNAS, "${Media.IS_MUSIC} != 0", null, null)?.use { c ->
            val id = c.getColumnIndexOrThrow(Media._ID)
            val title = c.getColumnIndexOrThrow(Media.TITLE)
            val artist = c.getColumnIndexOrThrow(Media.ARTIST)
            val album = c.getColumnIndexOrThrow(Media.ALBUM)
            val duration = c.getColumnIndexOrThrow(Media.DURATION)
            val name = c.getColumnIndexOrThrow(Media.DISPLAY_NAME)
            val albumId = c.getColumnIndexOrThrow(Media.ALBUM_ID)
            val albumArtist = c.getColumnIndexOrThrow(Media.ALBUM_ARTIST)
            val track = c.getColumnIndexOrThrow(Media.TRACK)
            val year = c.getColumnIndexOrThrow(Media.YEAR)
            buildList {
                while (c.moveToNext()) {
                    add(
                        songFrom(
                            c.getLong(id), c.getString(title), c.getString(artist),
                            c.getString(album), c.getLong(duration), c.getString(name),
                            c.getLong(albumId), c.getString(albumArtist), c.getInt(track), c.getInt(year),
                        ),
                    )
                }
            }
        }.orEmpty().sortedWith(compareBy(TitleOrder.comparator) { it.title })
    } catch (_: SecurityException) {
        emptyList() // sin permiso: la interfaz ya muestra Permisos
    }

    private companion object {
        val COLUMNAS = arrayOf(
            Media._ID, Media.TITLE, Media.ARTIST, Media.ALBUM, Media.DURATION, Media.DISPLAY_NAME,
            Media.ALBUM_ID, Media.ALBUM_ARTIST, Media.TRACK, Media.YEAR,
        )
    }
}
