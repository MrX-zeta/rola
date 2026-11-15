package com.luis.rola.feature.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeTest {

    private val d = 2f // densidad
    private val ancho = 1000f
    private val rapida = SWIPE_VELOCIDAD_DP * d

    @Test
    fun `confirma por distancia en cualquier direccion`() {
        assertTrue(swipeCommits(-350f, 0f, ancho, d))
        assertTrue(swipeCommits(350f, 0f, ancho, d))
        assertFalse(swipeCommits(-349f, 0f, ancho, d))
    }

    @Test
    fun `confirma un lanzamiento corto y rapido`() {
        assertTrue(swipeCommits(-40f, -rapida, ancho, d))
        assertFalse(swipeCommits(-40f, -rapida + 1, ancho, d))
    }

    @Test
    fun `un lanzamiento en contra cancela aunque paso la distancia`() {
        assertFalse(swipeCommits(-600f, rapida, ancho, d))
        assertTrue(swipeCommits(-600f, rapida - 1, ancho, d)) // regreso lento: cuenta la distancia
    }

    @Test
    fun `sin desplazamiento no hay gesto`() {
        assertFalse(swipeCommits(0f, rapida * 3, ancho, d))
    }
}
