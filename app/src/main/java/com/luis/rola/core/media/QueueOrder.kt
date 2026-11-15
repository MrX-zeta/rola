package com.luis.rola.core.media

import androidx.media3.common.C
import kotlin.random.Random

/** Orden de reproducción como índices de la lista de Media3. Puro, para probarlo sin Android. */
object QueueOrder {

    /** [start] y los que le siguen según [next] hasta `C.INDEX_UNSET` (sin dar la vuelta). */
    fun follow(start: Int, next: (Int) -> Int): List<Int> =
        generateSequence(start) { i -> next(i).takeIf { it != C.INDEX_UNSET } }.toList()

    /** Mueve dentro de «A continuación»: [from] y [to] cuentan desde la canción en [currentPos]. */
    fun move(order: List<Int>, currentPos: Int, from: Int, to: Int): List<Int> =
        order.toMutableList().apply { add(currentPos + 1 + to, removeAt(currentPos + 1 + from)) }

    /** Aleatorio que empieza en [current]: así suena toda la cola y no solo lo que quedó detrás (FR-014). */
    fun shuffledStartingWith(current: Int, size: Int, random: Random): List<Int> =
        listOf(current) + (0 until size).filter { it != current }.shuffled(random)

    /** Frontera de confianza del comando `rola.ORDEN_ALEATORIO`: cada índice de la cola, una vez. */
    fun isPermutation(order: List<Int>, size: Int): Boolean = order.sorted() == List(size) { it }
}
