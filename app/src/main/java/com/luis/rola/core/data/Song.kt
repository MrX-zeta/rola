package com.luis.rola.core.data

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

/**
 * Un audio del MediaStore marcado como música. `artist`/`album`/`albumArtist` nulos = desconocidos.
 * [track] trae disco × 1000 + pista (0 = sin número); [year] 0 = sin año.
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val albumId: Long = 0,
    val albumArtist: String? = null,
    val track: Int = 0,
    val year: Int = 0,
) {
    val uri: Uri get() = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
}
