package com.luis.rola.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline

/**
 * Esqueleto de carga (DEC-09): forma del contenido en `surfaceContainer` con un brillo que cruza en línea recta.
 * Con «reducir movimiento» queda quieto (INV-06).
 */
@Composable
fun Modifier.esqueleto(shape: Shape): Modifier {
    val base = MaterialTheme.colorScheme.surfaceContainer
    val brillo = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f).compositeOver(base)
    val avance = if (LocalReduceMotion.current) {
        null
    } else {
        // Movimiento constante → lineal; 1.2 s por pasada es un indicador, no una transición de interfaz.
        rememberInfiniteTransition(label = "esqueleto").animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
            label = "brillo",
        )
    }
    return drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)
        val x = avance?.value
        if (x == null) {
            drawOutline(outline, base)
        } else {
            val ancho = size.width
            drawOutline(
                outline,
                Brush.linearGradient(
                    listOf(base, brillo, base),
                    start = Offset(ancho * x - ancho / 2, 0f),
                    end = Offset(ancho * x + ancho / 2, 0f),
                ),
            )
        }
    }
}
