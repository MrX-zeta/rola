package com.luis.rola.core.data

import android.provider.MediaStore

/** Fila del MediaStore → [Song]. Título vacío: nombre del archivo sin extensión. */
fun songFrom(
    id: Long,
    title: String?,
    artist: String?,
    album: String?,
    durationMs: Long,
    displayName: String?,
) = Song(
    id = id,
    title = title?.takeIf { it.isNotBlank() } ?: displayName.orEmpty().substringBeforeLast('.'),
    artist = artist.conocido(),
    album = album.conocido(),
    durationMs = durationMs.coerceAtLeast(0),
)

private fun String?.conocido() = this?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
