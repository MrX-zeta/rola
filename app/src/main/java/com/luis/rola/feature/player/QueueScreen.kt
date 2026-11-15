package com.luis.rola.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.core.media.QueueEntry
import com.luis.rola.feature.library.Cover
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * Cola (SCR-52, nodos 36:1557 y 51:4570): lo que suena y lo que sigue. Se reordena arrastrando el asa
 * (librería `reorderable`: mantiene compuesta la fila arrastrada y desplaza la lista en los bordes)
 * y se quita deslizando a la izquierda; tocar una fila la reproduce; «Limpiar» quita todo lo que sigue.
 * Los avisos con Deshacer los muestra quien llama (DEC-09, DEC-24).
 */
@Composable
fun QueueScreen(
    current: Song,
    colors: CoverColors,
    upNext: List<QueueEntry>,
    onBack: () -> Unit,
    onMove: (from: Int, to: Int) -> Boolean,
    onRemove: (position: Int) -> Unit,
    onPlay: (position: Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val m = MaterialTheme.colorScheme
    val reduce = LocalReduceMotion.current
    val lista = rememberLazyListState()
    // Orden de la vista mientras se arrastra y hasta que el reproductor publica la cola movida.
    var orden by remember { mutableStateOf<List<Long>?>(null) }
    var arrastrando by remember { mutableStateOf(false) }
    val actual by rememberUpdatedState(upNext)
    // Los gestos leen el estado al momento, no lo que se compuso: si la cola cambia a mitad del arrastre,
    // el orden se aplica sobre la nueva (FR-058).
    fun vista() = ordenComo(actual, orden.orEmpty())
    val filas = vista()
    LaunchedEffect(upNext) { if (!arrastrando) orden = null }
    // Solo las filas de «A continuación» son ReorderableItem: lo demás no es destino (FR-059).
    val reordenable = rememberReorderableLazyListState(
        lista,
        scrollThresholdPadding = WindowInsets.navigationBars.asPaddingValues(),
    ) { de, a ->
        orden = vista().map { it.song.id }.toMutableList().apply { add(indexOf(a.key as Long), removeAt(indexOf(de.key as Long))) }
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
                val pos = { actual.indexOfFirst { it.song.id == id } }
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
                ReorderableItem(
                    reordenable,
                    key = id,
                    modifier = Modifier.semantics {
                        customActions = buildList {
                            if (i > 0) add(CustomAccessibilityAction(subir) { onMove(i, i - 1) })
                            if (i < filas.lastIndex) add(CustomAccessibilityAction(bajar) { onMove(i, i + 1) })
                            add(CustomAccessibilityAction(quitar) { onRemove(i); true })
                        }
                    },
                    animateItemModifier = if (reduce) Modifier else Modifier.animateItem(),
                ) { esta ->
                    SwipeToDismissBox(
                        swipe,
                        backgroundContent = {
                            Box(Modifier.fillMaxSize().background(m.surfaceContainer).padding(end = RolaSpacing.xxl), Alignment.CenterEnd) {
                                Icon(painterResource(R.drawable.ic_delete), null, tint = m.onSurfaceVariant)
                            }
                        },
                        enableDismissFromStartToEnd = false,
                        gesturesEnabled = !reordenable.isAnyItemDragging,
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
                                    .draggableHandle(
                                        onDragStarted = {
                                            orden = vista().map { it.song.id }
                                            arrastrando = true
                                        },
                                        onDragStopped = {
                                            arrastrando = false
                                            val desde = pos()
                                            val hasta = vista().indexOfFirst { it.song.id == id }
                                            // Sin orden enviada la cola no cambiará: el orden de la vista se suelta ya.
                                            if (desde < 0 || hasta == desde || !onMove(desde, hasta)) orden = null
                                        },
                                    ),
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
}

/** [cola] en el orden de [ids]; las que no estaban van al final y las que ya no están se omiten (FR-058). */
internal fun ordenComo(cola: List<QueueEntry>, ids: List<Long>): List<QueueEntry> {
    val porId = cola.associateBy { it.song.id }
    val vistos = ids.toSet()
    return ids.mapNotNull { porId[it] } + cola.filter { it.song.id !in vistos }
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
