package com.luis.rola.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill

enum class Destino(val ruta: String, val icono: Int, val texto: Int) {
    Inicio("inicio", R.drawable.ic_home, R.string.nav_inicio),
    Biblioteca("biblioteca", R.drawable.ic_library, R.string.nav_biblioteca),
    Buscar("buscar", R.drawable.ic_search, R.string.nav_buscar),
}

private val ICONO = 24.dp

/**
 * Píldora de tres destinos (DEC-03): el activo con ícono y texto, los demás solo ícono.
 * DEC-28 `barra_navegacion`: la selección cambia en el mismo toque; ancho fijo; un solo progreso 0→1 por
 * pestaña decide su ancho, la opacidad de su etiqueta y el fondo lima, todo en el mismo cuadro.
 */
@Composable
fun RolaNavBar(actual: String?, onSelect: (Destino) -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.pill
    val acento = RolaTheme.colors.accent
    val reduce = LocalReduceMotion.current

    // Se adelanta a la navegación al tocar y se resincroniza cuando cambia la ruta (atrás del sistema, etc.).
    var elegido by remember(actual) { mutableStateOf(actual) }
    val progresos = Destino.entries.map { d ->
        val meta = if (d.ruta == elegido) 1f else 0f
        val anim = animateFloatAsState(meta, RolaMotion.o(reduce, RolaMotion.resorte()), label = "progreso")
        // Se leen como estado en medida y dibujo: el cambio relanza ambos sin depender de recomponer.
        if (reduce) { { if (d.ruta == elegido) 1f else 0f } } else { { anim.value.coerceIn(0f, 1f) } }
    }
    val geo = remember { GeometriaBarra() }

    Layout(
        content = {
            Destino.entries.forEachIndexed { i, d ->
                val activo = d.ruta == elegido
                Box(
                    Modifier
                        .clip(forma)
                        .selectable(activo, role = Role.Tab) {
                            elegido = d.ruta
                            onSelect(d)
                        },
                ) {
                    Row(
                        // La etiqueta conserva su tamaño: la pestaña la recorta, no la encoge.
                        Modifier
                            .wrapContentWidth(Alignment.Start, unbounded = true)
                            .padding(horizontal = RolaSpacing.md, vertical = RolaSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconoDestino(d, activo, progresos[i])
                        Text(
                            stringResource(d.texto),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = RolaTheme.colors.onAccent,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier
                                .padding(start = RolaSpacing.xs)
                                .graphicsLayer { alpha = progresos[i]() },
                        )
                    }
                }
            }
        },
        modifier = modifier
            .background(c.surface, forma)
            .border(1.dp, c.outline, forma)
            .drawBehind {
                val p = progresos.map { it() }
                geo.calcular(p)
                val suma = p.sum()
                if (suma > 0f) {
                    // Fondo lima: promedio de las pestañas pesado por su progreso.
                    val x = p.indices.sumOf { (p[it] * geo.izquierda[it]).toDouble() }.toFloat() / suma
                    val w = p.indices.sumOf { (p[it] * geo.ancho[it]).toDouble() }.toFloat() / suma
                    drawRoundRect(
                        acento,
                        topLeft = Offset(x, geo.pad.toFloat()),
                        size = Size(w, size.height - 2 * geo.pad),
                        cornerRadius = CornerRadius(size.height / 2),
                    )
                }
            }
            .selectableGroup(),
    ) { medibles, restricciones ->
        geo.pad = RolaSpacing.xxs.roundToPx()
        geo.hueco = RolaSpacing.xxs.roundToPx()
        geo.min = (RolaSpacing.md * 2 + ICONO).roundToPx()
        // Con letra grande en pantallas angostas, la etiqueta se recorta en vez de sacar pestañas de la barra.
        val cabe = (restricciones.maxWidth - 2 * geo.pad - geo.hueco * (medibles.size - 1) - geo.min * medibles.size)
            .coerceAtLeast(0)
        geo.extra = medibles.map { (it.maxIntrinsicWidth(Constraints.Infinity) - geo.min).coerceIn(0, cabe) }
        geo.calcular(progresos.map { it() })
        val colocables = medibles.mapIndexed { i, m -> m.measure(Constraints.fixedWidth(geo.ancho[i])) }
        val alto = colocables.maxOf { it.height } + 2 * geo.pad
        layout(geo.total.coerceAtMost(restricciones.maxWidth), alto) {
            colocables.forEachIndexed { i, p -> p.place(geo.izquierda[i], geo.pad) }
        }
    }
}

/**
 * Geometría pura de la barra. Ancho total fijo = el de la pestaña con la etiqueta más larga;
 * el espacio que sobra con etiquetas cortas se reparte entre los huecos.
 */
internal class GeometriaBarra {
    var pad = 0
    var hueco = 0
    var min = 0
    var extra: List<Int> = emptyList()
    var ancho = IntArray(0)
    var izquierda = IntArray(0)
    val total get() = 2 * pad + extra.size * min + (extra.maxOrNull() ?: 0) + (extra.size - 1).coerceAtLeast(0) * hueco

    fun calcular(progreso: List<Float>) {
        ancho = IntArray(extra.size) { min + (extra[it] * progreso[it]).toInt() }
        val sobra = (total - 2 * pad - ancho.sum()).coerceAtLeast(0)
        val huecos = (extra.size - 1).coerceAtLeast(1)
        var x = pad
        izquierda = IntArray(extra.size) { i ->
            val aqui = x
            x += ancho[i] + sobra / huecos + if (i < sobra % huecos) 1 else 0
            aqui
        }
    }
}

/** Color por el mismo progreso de la pestaña; al activarse, rebote corto de escala (DEC-28). */
@Composable
private fun IconoDestino(d: Destino, activo: Boolean, progreso: () -> Float) {
    val reduce = LocalReduceMotion.current
    val apagado = MaterialTheme.colorScheme.onSurfaceVariant
    val encendido = RolaTheme.colors.onAccent
    val escala = remember { Animatable(1f) }
    var antes by remember { mutableStateOf(activo) }
    LaunchedEffect(activo) {
        if (activo && !antes && !reduce) {
            escala.animateTo(1.1f, tween(RolaMotion.RAPIDA, easing = RolaMotion.EaseOut))
            escala.animateTo(1f, RolaMotion.resorte())
        }
        antes = activo
    }
    // Sin descripción: la etiqueta (siempre presente, aunque transparente) ya nombra la pestaña para TalkBack.
    val pintor = painterResource(d.icono)
    Box(
        Modifier
            .size(ICONO)
            .graphicsLayer {
                scaleX = escala.value
                scaleY = escala.value
            }
            .drawBehind {
                with(pintor) { draw(size, colorFilter = ColorFilter.tint(lerp(apagado, encendido, progreso()))) }
            },
    )
}
