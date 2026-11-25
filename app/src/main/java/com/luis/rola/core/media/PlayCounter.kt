package com.luis.rola.core.media

/** Cuándo cuenta una escucha (TBL-HISTORY): ≥ 30 s sonando, o terminar si dura menos; una vez por escucha. Puro. */
class PlayCounter(private val now: () -> Long) {

    private var id: Long? = null
    private var contada = false
    private var sonado = 0L
    private var desde: Long? = null // suena desde entonces; `null` en pausa

    private val total get() = sonado + (desde?.let { now() - it } ?: 0)

    /** Empieza otra escucha: otra canción, o la misma otra vez. */
    fun start(id: Long?) {
        this.id = id
        contada = false
        sonado = 0
        if (desde != null) desde = now()
    }

    fun playing(isPlaying: Boolean) {
        if (isPlaying == (desde != null)) return
        sonado = total
        desde = if (isPlaying) now() else null
    }

    /** Cuánto falta para contar; `null` si no suena o ya contó. */
    val remainingMs: Long? get() = if (contada || desde == null) null else (UMBRAL_MS - total).coerceAtLeast(0)

    /** La canción que acaba de llegar a 30 s, o `null`. */
    fun due(): Long? = contar(total >= UMBRAL_MS)

    /** La canción que terminó en [atMs] (su duración), si dura menos de 30 s y no había contado. */
    fun ended(atMs: Long): Long? = contar(total > 0 && atMs < UMBRAL_MS)

    private fun contar(toca: Boolean): Long? = id.takeIf { toca && !contada }?.also { contada = true }

    private companion object {
        const val UMBRAL_MS = 30_000L
    }
}
