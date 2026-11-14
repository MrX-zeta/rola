package com.luis.rola.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/** Relación de contraste WCAG 2.x entre dos colores (de 1.0 a 21.0). */
fun contrastRatio(a: Color, b: Color): Double {
    val la = a.luminance().toDouble()
    val lb = b.luminance().toDouble()
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
}

/**
 * [fg] si ya contrasta [min]:1 con [bg]; si no, lo acerca a blanco o negro (el que más contraste dé)
 * hasta cumplir (INV-06). Blanco o negro siempre llegan a 4.5 sobre cualquier fondo.
 */
fun ensureContrast(fg: Color, bg: Color, min: Double = 4.5): Color {
    if (contrastRatio(fg, bg) >= min) return fg
    val meta = if (contrastRatio(Color.White, bg) >= contrastRatio(Color.Black, bg)) Color.White else Color.Black
    return (1..20).map { lerp(fg, meta, it / 20f) }.firstOrNull { contrastRatio(it, bg) >= min } ?: meta
}
