package com.luis.rola.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class Destino(val ruta: String, val icono: Int, val texto: Int) {
    Inicio("inicio", R.drawable.ic_home, R.string.nav_inicio),
    Biblioteca("biblioteca", R.drawable.ic_library, R.string.nav_biblioteca),
    Buscar("buscar", R.drawable.ic_search, R.string.nav_buscar),
}

/**
 * Píldora de tres destinos (DEC-03): el activo con ícono y texto, los demás solo ícono.
 * El fondo lima se desliza con resorte hacia la pestaña nueva (DEC-28); con reducir movimiento, salta.
 */
@Composable
fun RolaNavBar(actual: String?, onSelect: (Destino) -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.pill
    val acento = RolaTheme.colors.accent
    val reduce = LocalReduceMotion.current

    val limites = remember { mutableStateMapOf<Destino, Rect>() }
    val objetivo = Destino.entries.firstOrNull { it.ruta == actual }?.let { limites[it] }
    val x = remember { Animatable(0f) }
    val ancho = remember { Animatable(0f) }
    LaunchedEffect(objetivo, reduce) {
        val o = objetivo ?: return@LaunchedEffect
        if (ancho.value == 0f || reduce) { // primera medida: sin animar desde cero
            x.snapTo(o.left)
            ancho.snapTo(o.width)
        } else {
            coroutineScope {
                launch { x.animateTo(o.left, RolaMotion.resorte()) }
                launch { ancho.animateTo(o.width, RolaMotion.resorte()) }
            }
        }
    }

    Row(
        modifier
            .background(c.surface, forma)
            .border(1.dp, c.outline, forma)
            .padding(RolaSpacing.xxs)
            .drawBehind {
                if (ancho.value > 0f) {
                    drawRoundRect(
                        acento,
                        topLeft = Offset(x.value, 0f),
                        size = Size(ancho.value, size.height),
                        cornerRadius = CornerRadius(size.height / 2),
                    )
                }
            }
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xxs),
    ) {
        Destino.entries.forEach { d ->
            val activo = d.ruta == actual
            Row(
                Modifier
                    .onGloballyPositioned { limites[d] = it.boundsInParent() }
                    .clip(forma)
                    .selectable(activo, role = Role.Tab) { onSelect(d) }
                    .padding(horizontal = RolaSpacing.md, vertical = RolaSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconoDestino(d, activo)
                AnimatedVisibility(
                    activo,
                    enter = if (reduce) {
                        fadeIn(tween(RolaMotion.MEDIA))
                    } else {
                        fadeIn(tween(RolaMotion.MEDIA, easing = RolaMotion.EaseOut)) +
                            expandHorizontally(tween(RolaMotion.LENTA, easing = RolaMotion.EaseOut), expandFrom = Alignment.Start)
                    },
                    exit = if (reduce) {
                        fadeOut(tween(RolaMotion.RAPIDA))
                    } else {
                        fadeOut(tween(RolaMotion.RAPIDA)) +
                            shrinkHorizontally(tween(200, easing = RolaMotion.EaseOut), shrinkTowards = Alignment.Start)
                    },
                ) {
                    Text(
                        stringResource(d.texto),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = RolaTheme.colors.onAccent,
                        maxLines = 1,
                        modifier = Modifier.padding(start = RolaSpacing.xs),
                    )
                }
            }
        }
    }
}

/** El ícono cambia de color con fundido y, al activarse, da un rebote corto de escala (DEC-28). */
@Composable
private fun IconoDestino(d: Destino, activo: Boolean) {
    val reduce = LocalReduceMotion.current
    val tinte by animateColorAsState(
        if (activo) RolaTheme.colors.onAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        RolaMotion.o(reduce, tween(RolaMotion.MEDIA, easing = RolaMotion.EaseOut)),
        label = "tinte",
    )
    val escala = remember { Animatable(1f) }
    var antes by remember { mutableStateOf(activo) }
    LaunchedEffect(activo) {
        if (activo && !antes && !reduce) {
            escala.animateTo(1.1f, tween(RolaMotion.RAPIDA, easing = RolaMotion.EaseOut))
            escala.animateTo(1f, RolaMotion.resorte())
        }
        antes = activo
    }
    Icon(
        painterResource(d.icono),
        contentDescription = if (activo) null else stringResource(d.texto),
        tint = tinte,
        modifier = Modifier.graphicsLayer {
            scaleX = escala.value
            scaleY = escala.value
        },
    )
}
