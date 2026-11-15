package com.luis.rola.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

/**
 * Estado de la capa tarjeta ↔ Reproduciendo. [expansion]: 0 = tarjeta, 1 = pantalla completa.
 * Se guarda si estaba abierta (y la cola) para sobrevivir a rotar o cambiar de tema.
 */
@Stable
class PlayerSheetState(abierto: Boolean, colaAbierta: Boolean) {
    val expansion = Animatable(if (abierto) 1f else 0f).apply { updateBounds(0f, 1f) }

    /** Límites de la mini tarjeta en la raíz: de ahí nace la capa. */
    var cardBounds by mutableStateOf(Rect.Zero)

    var queueOpen by mutableStateOf(colaAbierta)

    /** La capa existe mientras crece, está abierta o se pidió abrirla. */
    val visible by derivedStateOf { expansion.value > 0f || opening }

    /** Se pidió abrir con un toque: la capa se compone primero y luego anima (DEC-28 `arranque_en_primer_cuadro`). */
    var opening by mutableStateOf(false)
        private set

    fun requestOpen() {
        opening = true
    }

    /**
     * La capa sigue al dedo. Sin despachar: el salto se registra en el acto, así que nunca llega después de la
     * animación de soltar y la corta a medias (FR-003, revisión del Redmi).
     */
    fun follow(scope: CoroutineScope, p: Float) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) { expansion.snapTo(p) }
    }

    suspend fun open(reduce: Boolean, velocity: Float = 0f) {
        try {
            expansion.animateTo(1f, if (reduce) tween(RolaMotion.MEDIA) else RolaMotion.resorte(), velocity)
        } finally {
            opening = false
        }
    }

    /** Un arrastre de la tarjeta que no llegó al umbral: regresa. */
    suspend fun cancel(reduce: Boolean) {
        expansion.animateTo(0f, RolaMotion.o(reduce, RolaMotion.resorte()))
    }

    /** Flecha o Atrás: salida más corta que la entrada; al soltar un arrastre, resorte con su velocidad. */
    suspend fun close(reduce: Boolean, velocity: Float = 0f, fromDrag: Boolean = false) {
        val spec = when {
            reduce -> tween<Float>(RolaMotion.RAPIDA)
            fromDrag -> RolaMotion.resorte()
            else -> tween(200, easing = RolaMotion.EaseOut)
        }
        expansion.animateTo(0f, spec, velocity)
        queueOpen = false
    }

    companion object {
        val Saver = listSaver<PlayerSheetState, Boolean>(
            save = { listOf(it.expansion.targetValue > 0.5f, it.queueOpen) },
            restore = { PlayerSheetState(it[0], it[1]) },
        )
    }
}

@Composable
fun rememberPlayerSheetState() = rememberSaveable(saver = PlayerSheetState.Saver) { PlayerSheetState(false, false) }

/**
 * Transformación de contenedor (DEC-04, DEC-28): un recorte redondeado crece de la tarjeta a la pantalla
 * siguiendo [PlayerSheetState.expansion]. Todo se lee al dibujar: el gesto no recompone.
 * Con reducir movimiento, nada se recorta ni se mueve: fundido corto a pantalla completa (FR-005).
 */
@Composable
fun PlayerSheet(
    sheet: PlayerSheetState,
    cardColor: Color,
    card: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val conCopia by remember(sheet) { derivedStateOf { sheet.expansion.value < 0.5f } }
    if (!sheet.visible) return
    val reduce = LocalReduceMotion.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    LaunchedEffect(sheet.opening) {
        if (sheet.opening) {
            withFrameNanos {} // cuenta desde que la capa ya está dibujada
            sheet.open(reduce)
        }
    }
    BackHandler(enabled = !sheet.queueOpen) { scope.launch { sheet.close(reduce) } }
    var arrastre by remember { mutableFloatStateOf(0f) }
    var desde by remember { mutableFloatStateOf(1f) } // si se toma a medio camino, sigue desde ahí sin saltar
    val travel = sheet.cardBounds.top.coerceAtLeast(1f)
    val fondo = MaterialTheme.colorScheme.background
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val p = sheet.expansion.value
                if (reduce) {
                    alpha = p
                } else {
                    clip = true
                    shape = Recorte(lerp(sheet.cardBounds, Rect(Offset.Zero, size), p), lerp(20.dp.toPx(), 0f, p))
                }
            }
            .background(fondo)
            .draggable(
                rememberDraggableState { d ->
                    arrastre += d
                    if (!reduce) sheet.follow(scope, (desde - arrastre / travel).coerceIn(0f, 1f))
                },
                Orientation.Vertical,
                enabled = !sheet.queueOpen,
                onDragStarted = {
                    arrastre = 0f
                    desde = sheet.expansion.value
                },
                // También llega al cancelarse el gesto (velocidad 0): siempre termina cerrada o abierta.
                onDragStopped = { v ->
                    if (arrastre > 0f && swipeCommits(arrastre, v, travel, density.density)) {
                        sheet.close(reduce, -v / travel, fromDrag = true)
                    } else {
                        sheet.open(reduce)
                    }
                },
            ),
    ) {
        // Reproduciendo entra en fundido y acompaña el borde superior del recorte.
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                if (!reduce) {
                    val p = sheet.expansion.value
                    alpha = ((p - 0.2f) / 0.8f).coerceIn(0f, 1f)
                    translationY = sheet.cardBounds.top * (1f - p)
                }
            },
        ) { content() }
        // Pasado el 50 % ya no se ven: fuera de la composición para que no tapen los botones de arriba.
        if (!reduce && conCopia) {
            // El color de la tarjeta llena el recorte al principio y se desvanece a la mitad.
            Box(Modifier.fillMaxSize().drawBehind { drawRect(cardColor, alpha = (1f - sheet.expansion.value / 0.5f).coerceIn(0f, 1f)) })
            // Copia de la tarjeta, sin toques ni lector de pantalla: se desvanece en el primer 30 %.
            val ancho = with(density) { sheet.cardBounds.width.toDp() }
            Box(
                Modifier
                    .width(ancho)
                    .graphicsLayer {
                        val p = sheet.expansion.value
                        translationX = sheet.cardBounds.left * (1f - p)
                        translationY = sheet.cardBounds.top * (1f - p)
                        alpha = (1f - p / 0.3f).coerceIn(0f, 1f)
                    }
                    .clearAndSetSemantics {},
            ) { card() }
        }
    }
}

private class Recorte(val rect: Rect, val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Rounded(RoundRect(rect, CornerRadius(radius)))
}
