package com.luis.rola.feature.library

import com.luis.rola.core.data.Song
import com.luis.rola.core.data.TitleOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class AlphabetTest {

    @Test
    fun primeraFilaDeCadaLetra() {
        val titulos = listOf("Árbol", "avión", "Ñandú", "Nube", "9 crímenes").sortedWith(TitleOrder.comparator)
        val songs = titulos.mapIndexed { i, t -> Song(i.toLong(), t, null, null, 0) }
        assertEquals(mapOf("A" to 0, "N" to 2, "Ñ" to 3, "#" to 4), firstIndexBySection(songs))
    }

    @Test
    fun letrasEnOrdenConEñeYNumeral() {
        assertEquals(28, LETRAS.size)
        assertEquals("Ñ", LETRAS[LETRAS.indexOf("N") + 1])
        assertEquals("#", LETRAS.last())
    }
}
