package com.luis.rola.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.TitleOrder
import com.luis.rola.core.designsystem.RolaTheme

/** Secciones del abecedario en orden (DEC-05): Ñ entre N y O, «#» al final. */
val LETRAS: List<String> = ('A'..'N').map(Char::toString) + "Ñ" + ('O'..'Z').map(Char::toString) + "#"

/** Primera fila de cada sección presente en [songs] (ya ordenadas por título). */
fun firstIndexBySection(songs: List<Song>): Map<String, Int> {
    val m = LinkedHashMap<String, Int>()
    songs.forEachIndexed { i, s -> m.putIfAbsent(TitleOrder.sectionOf(s.title), i) }
    return m
}

private val ALTO_LETRA = 17.dp

/**
 * Abecedario vertical (DEC-05, Figma 35:1541). Tocar o arrastrar salta a la primera canción de la letra.
 * Las letras miden 17 dp: para el lector de pantalla es un solo control con una acción por letra.
 */
@Composable
fun AlphabetBar(firstIndex: Map<String, Int>, active: String?, onJump: (Int) -> Unit, modifier: Modifier = Modifier) {
    val res = LocalResources.current
    val salto = rememberUpdatedState { y: Float, altoPx: Float ->
        LETRAS.getOrNull((y / altoPx).toInt())?.let(firstIndex::get)?.let(onJump)
    }
    val descripcion = stringResource(R.string.abecedario)
    Column(
        modifier
            .width(32.dp) // más ancho que las letras para tocarlo; sin tapar las casillas
            .padding(vertical = 4.dp)
            .pointerInput(Unit) { detectTapGestures { salto.value(it.y, ALTO_LETRA.toPx()) } }
            .pointerInput(Unit) { detectVerticalDragGestures { c, _ -> salto.value(c.position.y, ALTO_LETRA.toPx()) } }
            .clearAndSetSemantics {
                contentDescription = descripcion
                customActions = LETRAS.filter { it in firstIndex }.map { l ->
                    CustomAccessibilityAction(res.getString(R.string.ir_a_letra, l)) { onJump(firstIndex.getValue(l)); true }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LETRAS.forEach { l ->
            val activa = l == active
            Box(
                Modifier
                    .size(width = 18.dp, height = ALTO_LETRA)
                    .then(if (activa) Modifier.background(RolaTheme.colors.accent, CircleShape) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    l,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium),
                    color = when {
                        activa -> RolaTheme.colors.onAccent
                        l in firstIndex -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.outline
                    },
                )
            }
        }
    }
}
