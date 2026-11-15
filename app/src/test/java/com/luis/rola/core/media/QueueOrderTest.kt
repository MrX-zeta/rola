package com.luis.rola.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QueueOrderTest {

    @Test
    fun `sigue el orden hasta que no hay siguiente`() {
        val siguientes = mapOf(2 to 0, 0 to 3, 3 to 1)
        assertEquals(listOf(2, 0, 3, 1), QueueOrder.follow(2) { siguientes[it] ?: -1 })
        assertEquals(listOf(4), QueueOrder.follow(4) { -1 })
    }

    @Test
    fun `mover solo toca lo que sigue a la actual`() {
        val orden = listOf(0, 1, 2, 3, 4) // suena el 1 (posición 1); siguen 2, 3, 4
        assertEquals(listOf(0, 1, 4, 2, 3), QueueOrder.move(orden, 1, from = 2, to = 0)) // bajar al primer lugar
        assertEquals(listOf(0, 1, 3, 4, 2), QueueOrder.move(orden, 1, from = 0, to = 2)) // al último
        assertEquals(orden, QueueOrder.move(orden, 1, from = 1, to = 1))
    }

    @Test
    fun `mover y deshacer quitar con el orden guardado deja la cola como estaba`() {
        val antes = listOf(3, 0, 4, 1, 2)
        val movido = QueueOrder.move(antes, 0, from = 3, to = 0)
        assertEquals(listOf(3, 2, 0, 4, 1), movido)
        assertEquals(antes, QueueOrder.move(movido, 0, from = 0, to = 3))
    }

    @Test
    fun `aleatorio empieza en la actual y no repite`() {
        repeat(20) { semilla ->
            val orden = QueueOrder.shuffledStartingWith(3, 10, Random(semilla))
            assertEquals(3, orden.first())
            assertTrue(QueueOrder.isPermutation(orden, 10))
        }
    }

    @Test
    fun `solo acepta permutaciones exactas`() {
        assertTrue(QueueOrder.isPermutation(listOf(2, 0, 1), 3))
        assertTrue(QueueOrder.isPermutation(emptyList(), 0))
        assertFalse(QueueOrder.isPermutation(listOf(0, 0, 1), 3))
        assertFalse(QueueOrder.isPermutation(listOf(0, 1), 3))
        assertFalse(QueueOrder.isPermutation(listOf(0, 1, 3), 3))
        assertFalse(QueueOrder.isPermutation(listOf(-1, 0, 1), 3))
    }

    @Test
    fun `limpiar agrupa en tramos contiguos sin importar el orden`() {
        assertEquals(listOf(3..6), QueueOrder.runs(listOf(3, 4, 5, 6))) // sin aleatorio: un solo tramo
        assertEquals(listOf(0..1, 3..3, 5..6), QueueOrder.runs(listOf(5, 0, 3, 1, 6))) // con aleatorio
        assertEquals(emptyList<IntRange>(), QueueOrder.runs(emptyList()))
    }
}
