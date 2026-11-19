package com.luis.rola.core.data

enum class SortBy { TITLE, ARTIST, ALBUM }

/** Orden de Canciones (DEC-05, KV-DATASTORE `sort_order`). Empate por título; Z→A invierte todo. */
data class SongSort(val by: SortBy = SortBy.TITLE, val ascending: Boolean = true) {

    /** Texto por el que se ordena y agrupa en el abecedario; desconocido = vacío (va en «#»). */
    fun field(song: Song): String = when (by) {
        SortBy.TITLE -> song.title
        SortBy.ARTIST -> song.artist.orEmpty()
        SortBy.ALBUM -> song.album.orEmpty()
    }

    val comparator: Comparator<Song> = compareBy(TitleOrder.comparator, ::field)
        .thenBy(TitleOrder.comparator) { it.title }
        .let { if (ascending) it else it.reversed() }

    fun sectionOf(song: Song): String = TitleOrder.sectionOf(field(song))

    fun key(): String = "${by}_${if (ascending) "ASC" else "DESC"}"

    companion object {
        /** Valor guardado → orden; desconocido o nulo = título A→Z. */
        fun parse(value: String?): SongSort {
            val by = SortBy.entries.find { value?.startsWith(it.name + "_") == true } ?: return SongSort()
            return when (value?.removePrefix(by.name + "_")) {
                "ASC" -> SongSort(by, true)
                "DESC" -> SongSort(by, false)
                else -> SongSort()
            }
        }
    }
}
