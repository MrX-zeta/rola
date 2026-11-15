package com.luis.rola.feature.player

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeTest {

    private val d = 2f // densidad
    private val rapida = SWIPE_VELOCIDAD_DP * d

    @Test
    fun `gana el menor entre el 25 por ciento y 96 dp en cualquier direccion`() {
        val ancho = 380f * d // tarjeta de 380 dp: el 25 % son 95 dp, menos que el tope de 96
        assertTrue(swipeCommits(-0.25f * ancho, 0f, ancho, d))
        assertTrue(swipeCommits(0.25f * ancho, 0f, ancho, d))
        assertFalse(swipeCommits(-0.25f * ancho + 1, 0f, ancho, d))
        val muyAncha = 2000f * d
        assertTrue(swipeCommits(-96f * d, 0f, muyAncha, d))
        assertFalse(swipeCommits(-96f * d + 1, 0f, muyAncha, d))
    }

    @Test
    fun `desde el centro de la tarjeta alcanza con recorrer el umbral`() {
        // Empezando en el centro de 380 dp quedan 190 dp hasta la orilla: el umbral (≤ 96 dp) cabe de sobra.
        val ancho = 380f * d
        assertTrue(swipeCommits(-100f * d, 0f, ancho, d))
    }

    @Test
    fun `confirma un lanzamiento corto y rapido`() {
        assertTrue(swipeCommits(-40f, -rapida, 1000f, d))
        assertFalse(swipeCommits(-40f, -rapida + 1, 1000f, d))
    }

    @Test
    fun `un lanzamiento en contra cancela aunque paso la distancia`() {
        assertFalse(swipeCommits(-600f, rapida, 1000f, d))
        assertTrue(swipeCommits(-600f, rapida - 1, 1000f, d)) // regreso lento: cuenta la distancia
    }

    @Test
    fun `sin desplazamiento no hay gesto`() {
        assertFalse(swipeCommits(0f, rapida * 3, 1000f, d))
    }
}
