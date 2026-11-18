package com.luis.rola.feature.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaTheme
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val PARTICULAS = 7
private const val RAFAGA_MS = 300

/**
 * Corazón de Reproduciendo (DEC-29, Figma 78:3335). Al marcar: relleno lima, rebote, ráfaga y háptica;
 * al desmarcar: contorno, sin ráfaga. Solo anima al tocar (no al cambiar de canción). Con reducir
 * movimiento solo cambia el relleno.
 */
@Composable
fun FavoriteButton(favorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val reduce = LocalReduceMotion.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val colors = RolaTheme.colors
    val escala = remember { Animatable(1f) }
    val rafaga = remember { Animatable(1f) } // 1 = terminada (no se dibuja)
    val descripcion = stringResource(R.string.favorita)
    Box(
        modifier
            .size(48.dp)
            .drawBehind {
                drawCircle(colors.overCover)
                val p = rafaga.value
                if (p >= 1f) return@drawBehind
                // Salen del corazón hasta pasar apenas el borde del círculo, se encogen y se apagan.
                val distancia = (10f + 16f * p).dp.toPx()
                repeat(PARTICULAS) { i ->
                    val a = 2 * PI * i / PARTICULAS - PI / 2
                    drawCircle(
                        colors.accent,
                        radius = (3f * (1f - p)).dp.toPx(),
                        center = center + Offset((cos(a) * distancia).toFloat(), (sin(a) * distancia).toFloat()),
                        alpha = 1f - p,
                    )
                }
            }
            .toggleable(
                value = favorite,
                interactionSource = null,
                indication = ripple(bounded = false),
                role = Role.Switch,
            ) { marcar ->
                onToggle()
                if (!marcar) {
                    scope.launch { rafaga.snapTo(1f) }
                    scope.launch { escala.animateTo(1f, RolaMotion.resorte()) }
                    return@toggleable
                }
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                if (reduce) return@toggleable
                // Cada toque retoma desde el valor actual: varios toques rápidos no se encolan.
                scope.launch {
                    escala.snapTo(0.8f)
                    escala.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 800f)) // ≈ 330 ms
                }
                scope.launch {
                    rafaga.snapTo(0f)
                    rafaga.animateTo(1f, tween(RAFAGA_MS, easing = RolaMotion.EaseOut))
                }
            }
            .semantics { contentDescription = descripcion }
            .testTag("favorita"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(if (favorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite),
            contentDescription = null,
            tint = if (favorite) colors.accent else colors.onOverCover,
            modifier = Modifier.size(24.dp).graphicsLayer {
                scaleX = escala.value
                scaleY = escala.value
            }.testTag("favorita_icono"), // dentro de la capa: las pruebas ven la escala
        )
    }
}
