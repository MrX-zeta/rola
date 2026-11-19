package com.luis.rola.feature.library

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.luis.rola.core.designsystem.RolaMotion

// Despliegue al entrar (DEC-28 despliegue_listas): 7 escalones × 25 ms + 125 ms por fila = 300 ms.
internal const val FILAS_CASCADA = 8
private const val ESCALON_MS = 25
private const val FILA_MS = 125
internal const val CASCADA_MS = (FILAS_CASCADA - 1) * ESCALON_MS + FILA_MS

/**
 * Fila o celda [indice] de la cascada: fundido + subida de 8 dp, a partir de la primera visible ([primera]).
 * [reloj] y [primera] solo se leen al dibujar: la cascada no recompone. Fuera de las 8, entran normales.
 */
fun Modifier.cascada(indice: Int, primera: () -> Int, reloj: Animatable<Float, AnimationVector1D>): Modifier = graphicsLayer {
    val k = indice - primera()
    val p = if (k in 0 until FILAS_CASCADA) {
        RolaMotion.EaseOut.transform(((reloj.value - k * ESCALON_MS) / FILA_MS).coerceIn(0f, 1f))
    } else {
        1f
    }
    alpha = p
    translationY = (1f - p) * 8.dp.toPx()
    // Sin capa fuera de pantalla por fila (8 por cuadro al entrar, medido en T02e).
    compositingStrategy = CompositingStrategy.ModulateAlpha
}
