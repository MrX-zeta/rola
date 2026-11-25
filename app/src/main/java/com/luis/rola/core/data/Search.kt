package com.luis.rola.core.data

/** Un resultado y el tramo de su nombre que coincide, sobre el texto original. */
data class Found<T>(val item: T, val match: IntRange)

/** Hasta 5 por grupo, primero los que empiezan con lo buscado (FR-032, FR-034). */
data class SearchResults(
    val query: String,
    val songs: List<Found<Song>>,
    val albums: List<Found<Album>>,
    val artists: List<Found<Artist>>,
    val playlists: List<Found<PlaylistSummary>>,
) {
    val isEmpty get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()
}

/** Texto normalizado como [TitleOrder] (sin mayúsculas ni acentos, «ñ» aparte) que recuerda de qué carácter viene cada uno. */
internal class SearchableText(private val original: String) {
    private val origen = ArrayList<Int>(original.length)
    private val texto = buildString {
        original.forEachIndexed { i, ch ->
            val n = TitleOrder.normalizar(ch.toString())
            append(n)
            repeat(n.length) { origen += i }
        }
    }

    /** Primera coincidencia de [query] (ya normalizada y no vacía), o `null`. */
    fun find(query: String): IntRange? {
        val s = texto.indexOf(query)
        if (s < 0) return null
        val e = s + query.length
        // Hasta donde empieza lo siguiente: así se lleva los acentos sueltos de la última letra.
        val fin = maxOf(origen[e - 1], (origen.getOrNull(e) ?: original.length) - 1)
        return origen[s]..fin
    }

    fun startsWith(query: String) = texto.startsWith(query)
}

/** Nombres normalizados una vez por biblioteca. Las listas llegan en orden DEC-05; los desconocidos (sin nombre) no se buscan. */
class LibraryIndex(songs: List<Song>, albums: List<Album>, artists: List<Artist>, playlists: List<PlaylistSummary>) {

    private class Entry<T>(val item: T, val name: SearchableText)

    private val songs = entries(songs) { it.title }
    private val albums = entries(albums) { it.name }
    private val artists = entries(artists) { it.name }
    private val playlists = entries(playlists) { it.name }

    private fun <T> entries(items: List<T>, name: (T) -> String?) = items.mapNotNull { i -> name(i)?.let { Entry(i, SearchableText(it)) } }

    /** `null` si no hay nada que buscar. */
    fun search(query: String): SearchResults? {
        val q = TitleOrder.normalizar(query.trim())
        if (q.isEmpty()) return null
        return SearchResults(query.trim(), find(songs, q), find(albums, q), find(artists, q), find(playlists, q))
    }

    private fun <T> find(entries: List<Entry<T>>, q: String): List<Found<T>> =
        entries.mapNotNull { e -> e.name.find(q)?.let { e to it } }
            .sortedBy { (e, _) -> !e.name.startsWith(q) } // estable: conserva el orden DEC-05
            .take(5)
            .map { (e, match) -> Found(e.item, match) }
}
