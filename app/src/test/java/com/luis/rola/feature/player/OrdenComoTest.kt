package com.luis.rola.feature.player

import com.luis.rola.core.data.Song
import com.luis.rola.core.media.QueueEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class OrdenComoTest {

    private fun cola(vararg ids: Long) = ids.mapIndexed { i, id -> QueueEntry(Song(id, "c$id", null, null, 0), i + 1) }
    private fun ids(l: List<QueueEntry>) = l.map { it.song.id }

    @Test
    fun sinOrdenQuedaLaCola() = assertEquals(listOf(1L, 2, 3), ids(ordenComo(cola(1, 2, 3), emptyList())))

    @Test
    fun aplicaElOrdenDelArrastre() = assertEquals(listOf(3L, 1, 2), ids(ordenComo(cola(1, 2, 3), listOf(3, 1, 2))))

    @Test
    fun laQuePasoASonarSeOmiteYElRestoConservaElOrden() =
        // Terminó la canción a mitad del arrastre: la 1 pasó a «Sonando ahora».
        assertEquals(listOf(3L, 2, 4), ids(ordenComo(cola(2, 3, 4), listOf(3, 1, 2, 4))))

    @Test
    fun lasNuevasVanAlFinal() = assertEquals(listOf(2L, 1, 9), ids(ordenComo(cola(1, 2, 9), listOf(2, 1))))
}
