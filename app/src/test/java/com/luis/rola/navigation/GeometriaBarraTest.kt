package com.luis.rola.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometriaBarraTest {

    private fun geo() = GeometriaBarra().apply {
        pad = 4
        hueco = 4
        min = 56
        extra = listOf(50, 80, 60) // Inicio, Biblioteca, Buscar
    }

    @Test
    fun anchoTotalFijoSinImportarLaPestana() {
        val g = geo()
        val esperado = 2 * 4 + 3 * 56 + 80 + 2 * 4
        for (p in listOf(listOf(1f, 0f, 0f), listOf(0f, 1f, 0f), listOf(0f, 0f, 1f), listOf(.3f, .7f, 0f))) {
            g.calcular(p)
            assertEquals(esperado, g.total)
            // La última pestaña termina justo en el borde interior.
            assertEquals(esperado - 4, g.izquierda[2] + g.ancho[2])
        }
    }

    @Test
    fun anchoDeCadaPestanaSigueSuProgreso() {
        val g = geo()
        g.calcular(listOf(0f, .5f, 1f))
        assertEquals(listOf(56, 96, 116), g.ancho.toList())
    }

    @Test
    fun huecosNuncaMenoresAlMinimo() {
        val g = geo()
        g.calcular(listOf(0f, 1f, 0f))
        for (i in 1..2) assertTrue(g.izquierda[i] - (g.izquierda[i - 1] + g.ancho[i - 1]) >= 4)
    }
}
