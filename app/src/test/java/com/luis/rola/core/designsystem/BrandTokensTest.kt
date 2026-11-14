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

    @Test
    fun `accent y onAccent son iguales en claro y oscuro`() {
        // RolaColors no depende del tema: un solo valor sirve para ambos.
        assertEquals(Color(0xFFC7FF2E), RolaColors().accent)
        assertEquals(Color(0xFF10100E), RolaColors().onAccent)
    }
}
