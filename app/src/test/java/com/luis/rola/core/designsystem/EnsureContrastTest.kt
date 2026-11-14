package com.luis.rola.core.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureContrastTest {

    @Test
    fun `un color que ya cumple no cambia`() {
        assertEquals(Color.White, ensureContrast(Color.White, Color.Black))
    }

    @Test
    fun `colores parecidos se ajustan hasta 4_5`() {
        val fondos = listOf(Color(0xFF1B2550), Color(0xFF777777), Color(0xFFDCE3FF), Color(0xFF2F4BB8))
        val textos = listOf(Color(0xFF2F4BB8), Color(0xFF808080), Color(0xFF9FB4FF), Color(0xFFC7FF2E))
        for (bg in fondos) for (fg in textos) {
            val r = contrastRatio(ensureContrast(fg, bg), bg)
            assertTrue("$fg sobre $bg: $r", r >= 4.5)
        }
    }
}
