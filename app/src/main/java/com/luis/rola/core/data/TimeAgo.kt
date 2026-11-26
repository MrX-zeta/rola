package com.luis.rola.core.data

/** Tramo de «hace …» para «Último escaneo» (FR-043). */
sealed interface TimeAgo {
    data object Moment : TimeAgo
    data class Minutes(val n: Int) : TimeAgo
    data class Hours(val n: Int) : TimeAgo
    data class Days(val n: Int) : TimeAgo
}

/** Hacia abajo; un reloj atrasado (fecha futura) cuenta como un momento. */
fun timeAgo(now: Long, then: Long): TimeAgo {
    val minutos = (now - then) / 60_000
    return when {
        minutos < 1 -> TimeAgo.Moment
        minutos < 60 -> TimeAgo.Minutes(minutos.toInt())
        minutos < 24 * 60 -> TimeAgo.Hours((minutos / 60).toInt())
        else -> TimeAgo.Days((minutos / (24 * 60)).toInt())
    }
}
