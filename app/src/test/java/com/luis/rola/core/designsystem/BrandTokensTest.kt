package com.luis.rola.core.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/** Avisa si alguien cambia por error los colores de marca (DEC-16). */
class BrandTokensTest {

    @Test
    fun `primary oscuro es el lima de marca`() {
        assertEquals(Color(0xFFC7FF2E), RolaDarkColors.primary)
    }

    @Test
    fun `background oscuro es el neutro calido`() {
        assertEquals(Color(0xFF10100E), RolaDarkColors.background)
    }
}
