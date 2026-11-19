package com.luis.rola.core.data

/** Álbum del sistema (uno por `albumId`). [name]/[artist] nulos = desconocidos; [year] 0 = sin año. */
data class Album(val id: Long, val name: String?, val artist: String?, val year: Int, val songs: List<Song>) {
    val durationMs: Long get() = songs.sumOf { it.durationMs }
}

/** Artista por `albumArtist ?: artist`; [name] nulo = «Artista desconocido». [albums]: el más reciente primero. */
data class Artist(val name: String?, val albums: List<Album>, val songs: List<Song>) {
    /** Portada de su álbum más reciente. */
    val cover: Song? get() = albums.firstOrNull()?.songs?.firstOrNull()
}

/** Álbumes y artistas derivados de las canciones (research §2, §4); se ordenan por nombre con DEC-05. */
object LibraryGrouping {

    /** Pista (disco × 1000 + número) con las que no tienen número al final; empate por título. */
    private val porPista: Comparator<Song> =
        compareBy<Song> { it.track == 0 }.thenBy { it.track }.thenBy(TitleOrder.comparator) { it.title }

    /** Más reciente primero; sin año o empate, por nombre. */
    private val recientes: Comparator<Album> =
        compareByDescending<Album> { it.year }.thenBy(TitleOrder.comparator) { it.name.orEmpty() }

    private fun <T> porNombre(nombre: (T) -> String?): Comparator<T> = compareBy(TitleOrder.comparator) { nombre(it).orEmpty() }

    fun albums(songs: List<Song>): List<Album> = songs.groupBy { it.albumId }.map { (id, canciones) ->
        val orden = canciones.sortedWith(porPista)
        Album(
            id = id,
            name = orden.firstNotNullOfOrNull { it.album },
            artist = orden.firstNotNullOfOrNull { it.albumArtist } ?: orden.first().artist,
            year = orden.maxOf { it.year },
            songs = orden,
        )
    }.sortedWith(porNombre { it.name })

    fun artists(songs: List<Song>, albums: List<Album> = albums(songs)): List<Artist> {
        val albumDe = albums.associateBy { it.id }
        return songs.groupBy { it.albumArtist ?: it.artist }.map { (nombre, canciones) ->
            val suyos = canciones.map { it.albumId }.distinct().mapNotNull(albumDe::get).sortedWith(recientes)
            val ids = canciones.mapTo(HashSet()) { it.id }
            Artist(nombre, suyos, suyos.flatMap { a -> a.songs.filter { it.id in ids } })
        }.sortedWith(porNombre { it.name })
    }
}
