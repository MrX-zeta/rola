package com.luis.rola.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Relación de contraste WCAG 2.x entre dos colores (de 1.0 a 21.0). */
fun contrastRatio(a: Color, b: Color): Double {
    val la = a.luminance().toDouble()
    val lb = b.luminance().toDouble()
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}
