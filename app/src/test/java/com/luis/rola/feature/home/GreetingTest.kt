package com.luis.rola.feature.home

import com.luis.rola.R
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GreetingTest {

    @Test
    fun `el saludo cambia a las 5, a las 12 y a las 19`() {
        assertEquals((5..11).toList(), (0..23).filter { greeting(it) == R.string.buenos_dias })
        assertEquals((12..18).toList(), (0..23).filter { greeting(it) == R.string.buenas_tardes })
        assertEquals((0..4) + (19..23), (0..23).filter { greeting(it) == R.string.buenas_noches })
    }

    @Test
    fun `la fecha va en espanol y con mayuscula`() {
        assertEquals("Viernes, 20 de noviembre", dateText(LocalDateTime.of(2026, 11, 20, 21, 30)))
        assertEquals("Miércoles, 4 de marzo", dateText(LocalDateTime.of(2026, 3, 4, 0, 0)))
    }
}
