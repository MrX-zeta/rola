package com.luis.rola.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.core.media.QueueEntry
import com.luis.rola.feature.library.Cover

/**
 * Cola (SCR-52, nodos 36:1557 y 51:4570): lo que suena y lo que sigue. Se reordena arrastrando el asa
 * y se quita deslizando a la izquierda; tocar una fila la reproduce; «Limpiar» quita todo lo que sigue.
 * Los avisos con Deshacer los muestra quien llama (DEC-09, DEC-24).
 */
@Composable
fun QueueScreen(
    current: Song,
    colors: CoverColors,
    upNext: List<QueueEntry>,
    onBack: () -> Unit,
    onMove: (from: Int, to: Int) -> Unit,
    onRemove: (position: Int) -> Unit,
    onPlay: (position: Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val m = MaterialTheme.colorScheme
    val reduce = LocalReduceMotion.current
    val lista = rememberLazyListState()
    // Orden de vista previa solo mientras dura un arrastre; la cola real vive en el reproductor.
    var preview by remember { mutableStateOf<List<QueueEntry>?>(null) }
    var arrastrando by remember { mutableStateOf<Long?>(null) }
    var desde by remember { mutableIntStateOf(-1) }
    val offsetY = remember { mutableFloatStateOf(0f) }
    // El arrastre vive en un pointerInput que se crea una vez por fila: lee siempre la cola y la orden actuales.
    val actual by rememberUpdatedState(upNext)
    val mover by rememberUpdatedState(onMove)
    // shortcut: si el reproductor rechaza la orden, la vista previa queda hasta el próximo cambio de cola o arrastre.
    LaunchedEffect(upNext) { preview = null }
    val filas = preview ?: upNext

    /**
     * Pone la fila [id] donde está su centro. Más allá de la primera o la última fila queda en la posición válida
     * más cercana: nunca sobre «Sonando ahora» ni fuera de la lista (FR-053).
     */
    fun reordenar(id: Long) {
        val info = lista.layoutInfo.visibleItemsInfo
        val yo = info.find { it.key == id } ?: return
        val centro = yo.offset + offsetY.floatValue + yo.size / 2
        val l = (preview ?: actual).toMutableList()
        val visibles = info.filter { it.key is Long }
        val otro = visibles.find { centro.toInt() in it.offset until it.offset + it.size }
            ?: visibles.first().takeIf { centro < it.offset && it.key == l.first().song.id }
            ?: visibles.last().takeIf { centro >= it.offset + it.size && it.key == l.last().song.id }
            ?: return
        if (otro.key == id) return
        val primera = lista.firstVisibleItemIndex to lista.firstVisibleItemScrollOffset
        l.add(l.indexOfFirst { it.song.id == otro.key }, l.removeAt(l.indexOfFirst { it.song.id == id }))
        preview = l
        offsetY.floatValue -= otro.offset - yo.offset // sigue bajo el dedo
        // La lista ancla su primera fila visible por clave: si se movió esa, salta y la fila se pierde. Se pide la misma
        // posición en el próximo cuadro, sin corrutina: un scrollToItem cancelaría el desplazamiento automático.
        if (yo.index == primera.first || otro.index == primera.first) {
            lista.requestScrollToItem(primera.first, primera.second)
        }
    }

    /** Mantiene la fila [id] dentro de la vista y entre el primer y el último lugar de la cola (FR-057). */
    fun limitar(id: Long) {
        val info = lista.layoutInfo
        val yo = info.visibleItemsInfo.find { it.key == id } ?: return
        val l = preview ?: actual
        val i = l.indexOfFirst { it.song.id == id }
        var min = (info.viewportStartOffset - yo.offset).toFloat()
        var max = (info.viewportEndOffset - info.afterContentPadding - yo.offset - yo.size).toFloat()
        if (i == 0) min = maxOf(min, 0f)
        if (i == l.lastIndex) max = minOf(max, 0f)
        offsetY.floatValue = offsetY.floatValue.coerceIn(min, maxOf(min, max))
    }

    // Cerca de un borde, la lista se desplaza sola mientras se arrastra (más rápido cuanto más cerca).
    val borde = with(LocalDensity.current) { 64.dp.toPx() }
    val maxima = with(LocalDensity.current) { 800.dp.toPx() } // px/s
    LaunchedEffect(arrastrando) {
        val id = arrastrando ?: return@LaunchedEffect
        var antes = withFrameNanos { it }
        while (true) {
            val ahora = withFrameNanos { it }
            val dt = (ahora - antes) / 1e9f
            antes = ahora
            val info = lista.layoutInfo
            val yo = info.visibleItemsInfo.find { it.key == id } ?: continue
            val arriba = yo.offset + offsetY.floatValue
            val abajo = arriba + yo.size
            val hacia = when {
                arriba < info.viewportStartOffset + borde -> (arriba - info.viewportStartOffset - borde) / borde
                abajo > info.viewportEndOffset - borde -> (abajo - info.viewportEndOffset + borde) / borde
                else -> 0f
            }.coerceIn(-1f, 1f)
            // Se detiene en el inicio y el final (FR-057).
            if (hacia == 0f || (hacia > 0 && !lista.canScrollForward) || (hacia < 0 && !lista.canScrollBackward)) continue
            offsetY.floatValue += lista.scrollBy(hacia * maxima * dt) // la fila sigue bajo el dedo
            limitar(id)
            reordenar(id)
        }
    }

    Column(modifier.fillMaxSize().background(m.background).statusBarsPadding().testTag("cola")) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).padding(start = RolaSpacing.xs, end = RolaSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onBack) { Icon(painterResource(R.drawable.ic_cerrar), stringResource(R.string.volver), tint = m.onSurface) }
            Text(stringResource(R.string.cola), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = m.onSurface)
            TextButton(onClear, enabled = upNext.isNotEmpty(), colors = ButtonDefaults.textButtonColors(contentColor = m.primary)) {
                Text(stringResource(R.string.limpiar), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
            }
        }
        LazyColumn(
            state = lista,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        ) {
            item(key = "ahora") { Etiqueta(stringResource(R.string.sonando_ahora)) }
            item(key = "actual") {
                Row(
                    Modifier
                        .padding(horizontal = RolaSpacing.sm)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.container)
                        .padding(start = RolaSpacing.xs, end = RolaSpacing.md, top = RolaSpacing.xs, bottom = RolaSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Textos(current, Modifier.weight(1f), colors.accent, colors.contentVariant)
                    Ecualizador(colors)
                }
            }
            item(key = "siguen") { Etiqueta(stringResource(R.string.a_continuacion, filas.size)) }
            if (filas.isEmpty()) {
                item(key = "vacia") {
                    Text(
                        stringResource(R.string.sin_mas_canciones),
                        Modifier.padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = m.onSurfaceVariant,
                    )
                }
            }
            // shortcut: la clave es el id de la canción; la cola de Biblioteca no repite canciones. Si llega una
            // cola con repetidas (listas, tarea 05), usar una clave por aparición.
            itemsIndexed(filas, key = { _, e -> e.song.id }) { i, e ->
                val id = e.song.id
                val esta = arrastrando == id
                val pos = { (preview ?: actual).indexOfFirst { it.song.id == id } }
                val subir = stringResource(R.string.subir)
                val bajar = stringResource(R.string.bajar)
                val quitar = stringResource(R.string.quitar_de_cola)
                val reproducir = stringResource(R.string.reproducir)
                // Sin rememberSaveable: guardado por clave, al deshacer la fila volvía «descartada» y se quitaba otra vez.
                val umbral = SwipeToDismissBoxDefaults.positionalThreshold
                val swipe = remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, umbral) }
                LaunchedEffect(swipe.currentValue) {
                    if (swipe.currentValue == SwipeToDismissBoxValue.EndToStart) onRemove(pos())
                }
                SwipeToDismissBox(
                    swipe,
                    backgroundContent = {
                        Box(Modifier.fillMaxSize().background(m.surfaceContainer).padding(end = RolaSpacing.xxl), Alignment.CenterEnd) {
                            Icon(painterResource(R.drawable.ic_delete), null, tint = m.onSurfaceVariant)
                        }
                    },
                    modifier = Modifier
                        .then(if (esta || reduce) Modifier.animateItem(null, null, null) else Modifier.animateItem())
                        .zIndex(if (esta) 1f else 0f)
                        .graphicsLayer { if (esta) translationY = offsetY.floatValue }
                        .semantics {
                            customActions = buildList {
                                if (i > 0) add(CustomAccessibilityAction(subir) { onMove(i, i - 1); true })
                                if (i < filas.lastIndex) add(CustomAccessibilityAction(bajar) { onMove(i, i + 1); true })
                                add(CustomAccessibilityAction(quitar) { onRemove(i); true })
                            }
                        },
                    enableDismissFromStartToEnd = false,
                    gesturesEnabled = arrastrando == null,
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(if (esta) m.surfaceContainer else m.background)
                            .clickable(onClickLabel = reproducir) { onPlay(pos()) }
                            .padding(start = RolaSpacing.lg, end = RolaSpacing.xs, top = RolaSpacing.xs, bottom = RolaSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Textos(e.song, Modifier.weight(1f), m.onSurface, m.onSurfaceVariant)
                        Box(
                            Modifier
                                .size(48.dp)
                                .pointerInput(Unit) { detectTapGestures {} } // tocar el asa no reproduce la fila
                                .pointerInput(id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            preview = actual
                                            desde = pos()
                                            arrastrando = id
                                        },
                                        onDrag = { ch, d ->
                                            ch.consume()
                                            offsetY.floatValue += d.y
                                            limitar(id)
                                            reordenar(id)
                                        },
                                        onDragEnd = {
                                            val hasta = pos()
                                            if (hasta != desde) mover(desde, hasta) else preview = null
                                            arrastrando = null
                                            offsetY.floatValue = 0f
                                        },
                                        onDragCancel = {
                                            preview = null
                                            arrastrando = null
                                            offsetY.floatValue = 0f
                                        },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(R.drawable.ic_drag_handle), stringResource(R.string.arrastrar_para_mover), tint = m.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto,
        Modifier.padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = 14.dp, bottom = 6.dp),
        style = MaterialTheme.typography.timeSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun Textos(song: Song, modifier: Modifier, titulo: Color, artista: Color) {
    Cover(song, Modifier.size(48.dp), shape = MaterialTheme.shapes.extraSmall)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            song.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = titulo,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            song.artist ?: stringResource(R.string.artista_desconocido),
            style = MaterialTheme.typography.bodyMedium,
            color = artista,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Tres barras fijas de Figma: marcan la que suena sin moverse (DEC-28: no decora). */
@Composable
private fun Ecualizador(colors: CoverColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        listOf(10, 16, 8).forEach { h ->
            Box(Modifier.width(3.dp).height(h.dp).clip(MaterialTheme.shapes.extraSmall).background(colors.accent))
        }
    }
}
