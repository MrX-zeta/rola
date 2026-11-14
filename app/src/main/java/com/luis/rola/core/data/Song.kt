package com.luis.rola.core.data

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

/** Un audio del MediaStore marcado como música. `artist`/`album` nulos = desconocidos. */
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
) {
    val uri: Uri get() = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
}
