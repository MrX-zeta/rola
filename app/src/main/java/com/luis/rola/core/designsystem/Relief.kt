package com.luis.rola.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * Relieve de DEC-27 (neumorfismo suave). Solo para onboarding, Sin canciones, Buscar y Sin letra.
 * Oscuro: valores de DEC-27 tal cual. Claro: DEC-27 solo da rangos (negro 10–16 %, luz 80–90 %);
 * se tomó negro 12 % y luz 85 % hasta que Figma fije los valores exactos.
 */
@Composable
private fun esOscuro() = MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun sombra(alpha: Float, x: Float, y: Float, radio: Float, color: Color) =
    Shadow(radius = radio.dp, color = color, offset = DpOffset(x.dp, y.dp), alpha = alpha)

@Composable
fun Modifier.elevado(shape: Shape): Modifier {
    val o = esOscuro()
    return this
        .dropShadow(shape, sombra(if (o) .75f else .12f, 6f, 8f, 20f, Color.Black))
        .dropShadow(shape, sombra(if (o) .06f else .85f, -4f, -4f, 12f, Color.White))
        .innerShadow(shape, sombra(if (o) .08f else .85f, 0f, 1f, 1f, Color.White))
}

@Composable
fun Modifier.hundido(shape: Shape): Modifier {
    val o = esOscuro()
    return this
        .innerShadow(shape, sombra(if (o) .70f else .12f, 2f, 3f, 6f, Color.Black))
        .innerShadow(shape, sombra(if (o) .06f else .85f, -1f, -2f, 4f, Color.White))
}

@Composable
fun Modifier.acento(shape: Shape): Modifier {
    val o = esOscuro()
    return this
        .dropShadow(shape, sombra(if (o) .60f else .16f, 0f, 8f, 20f, Color.Black))
        .dropShadow(shape, sombra(if (o) .05f else .90f, -4f, -4f, 12f, Color.White))
        .innerShadow(shape, sombra(if (o) .45f else .80f, 0f, 1.5f, 1f, Color.White))
        .innerShadow(shape, sombra(if (o) .14f else .10f, 0f, -3f, 6f, Color.Black))
}
