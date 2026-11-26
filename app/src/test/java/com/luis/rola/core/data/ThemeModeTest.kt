package com.luis.rola.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `lee los tres valores guardados`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.parse("system"))
        assertEquals(ThemeMode.DARK, ThemeMode.parse("dark"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.parse("light"))
    }

    @Test
    fun `desconocido vacio o nulo es oscuro`() {
        assertEquals(ThemeMode.DARK, ThemeMode.parse(null))
        assertEquals(ThemeMode.DARK, ThemeMode.parse(""))
        assertEquals(ThemeMode.DARK, ThemeMode.parse("LIGHT"))
    }

    @Test
    fun `ida y vuelta`() = ThemeMode.entries.forEach { assertEquals(it, ThemeMode.parse(it.key())) }
}
