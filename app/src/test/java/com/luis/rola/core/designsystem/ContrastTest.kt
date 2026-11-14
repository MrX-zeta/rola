package com.luis.rola.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {

    private val esquemas = mapOf("oscuro" to RolaDarkColors, "claro" to RolaLightColors)

    private val pares: List<Triple<String, (ColorScheme) -> Color, (ColorScheme) -> Color>> = listOf(
        Triple("onSurface/background", { it.onSurface }, { it.background }),
        Triple("onSurfaceVariant/surface", { it.onSurfaceVariant }, { it.surface }),
        Triple("onSurfaceVariant/surfaceContainer", { it.onSurfaceVariant }, { it.surfaceContainer }),
        Triple("primary/background", { it.primary }, { it.background }),
        Triple("onPrimaryContainer/primaryContainer", { it.onPrimaryContainer }, { it.primaryContainer }),
    )

    @Test
    fun `los pares de texto cumplen 4_5 en ambos esquemas`() {
        for ((esquema, colores) in esquemas) {
            for ((nombre, texto, fondo) in pares) {
                val ratio = contrastRatio(texto(colores), fondo(colores))
                assertTrue("$nombre en $esquema: $ratio < 4.5", ratio >= 4.5)
            }
        }
    }

    @Test
    fun `contrastRatio coincide con los extremos de WCAG`() {
        assertEquals(21.0, contrastRatio(Color.Black, Color.White), 0.01)
        assertEquals(1.0, contrastRatio(Color.White, Color.White), 0.001)
    }
}
