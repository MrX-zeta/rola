package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleOrderTest {

    private fun ordenar(vararg t: String) = t.sortedWith(TitleOrder.comparator)

    @Test
    fun `acentos agrupan con su letra`() {
        assertEquals(listOf("Árbol", "Arena", "Eco"), ordenar("Eco", "Arena", "Árbol"))
        assertEquals("A", TitleOrder.sectionOf("Árbol"))
    }

    @Test
    fun `la Ñ va entre N y O`() {
        assertEquals(listOf("Nube", "Ñandú", "Ola"), ordenar("Ola", "Ñandú", "Nube"))
        assertEquals(listOf("Nube", "Nuño", "Nz"), ordenar("Nz", "Nuño", "Nube"))
        assertEquals("Ñ", TitleOrder.sectionOf("ñandú"))
    }

    @Test
    fun `numeros y simbolos van en almohadilla al final`() {
        assertEquals(listOf("Zeta", "2 Minutos", "¿Quién?"), ordenar("¿Quién?", "Zeta", "2 Minutos"))
        assertEquals("#", TitleOrder.sectionOf("2 Minutos"))
        assertEquals("#", TitleOrder.sectionOf("¿Quién?"))
        assertEquals("#", TitleOrder.sectionOf(""))
    }

    @Test
    fun `sin distinguir mayusculas ni quitar articulos`() {
        assertEquals(listOf("agua", "Brillo", "El Sol", "faro"), ordenar("faro", "El Sol", "Brillo", "agua"))
        assertEquals("E", TitleOrder.sectionOf("El Sol"))
    }
}
