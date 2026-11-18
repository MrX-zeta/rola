package com.luis.rola.core.data

/** Buscador de la hoja de agregar: sin mayúsculas ni acentos, por título, artista o álbum. */
object SearchText {
    fun matches(song: Song, query: String): Boolean {
        val q = TitleOrder.normalizar(query.trim())
        if (q.isEmpty()) return true
        return listOfNotNull(song.title, song.artist, song.album).any { TitleOrder.normalizar(it).contains(q) }
    }
}

/** Nombre de lista (SCR-30): recortado, 1 a [MAX] caracteres; `null` si queda vacío. */
object PlaylistName {
    const val MAX = 60

    fun clean(raw: String): String? = raw.trim().take(MAX).trimEnd().ifEmpty { null }
}
