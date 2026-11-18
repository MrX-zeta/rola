package com.luis.rola.feature.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.SearchText
import com.luis.rola.core.data.Song
import com.luis.rola.core.data.TitleOrder
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill
import com.luis.rola.feature.library.AlphabetBar
import com.luis.rola.feature.library.SongRow
import com.luis.rola.feature.library.firstIndexBySection
import kotlinx.coroutines.launch

/**
 * Hoja para agregar canciones (SCR-33, Figma 34:1205): buscador, abecedario (DEC-05) y casillas.
 * [already]: canciones que ya están en la lista; se ven marcadas y no se pueden tocar.
 * [onAdd] recibe las marcadas en el orden de la hoja (por título), no en el que se tocaron (FR-017).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSongsSheet(songs: List<Song>, already: Set<Long>, onAdd: (List<Long>) -> Unit, onDismiss: () -> Unit) {
    val c = MaterialTheme.colorScheme
    var consulta by rememberSaveable { mutableStateOf("") }
    val marcadas = rememberSaveable(saver = listSaver({ it.toList() }, { it.toMutableStateList() })) { mutableListOf<Long>().toMutableStateList() }
    val visibles by remember(songs) { derivedStateOf { songs.filter { SearchText.matches(it, consulta) } } }
    val primeras by remember(songs) { derivedStateOf { firstIndexBySection(visibles) } }
    val lista = rememberLazyListState()
    val activa by remember(songs) {
        derivedStateOf { visibles.getOrNull(lista.firstVisibleItemIndex)?.let { TitleOrder.sectionOf(it.title) } }
    }
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismiss, Modifier.testTag("hoja_agregar"), sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.navigationBarsPadding()) {
            Text(
                stringResource(R.string.agregar_canciones),
                style = MaterialTheme.typography.titleLarge,
                color = c.onSurface,
                modifier = Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, bottom = RolaSpacing.xs),
            )
            Buscador(consulta, { consulta = it })
            Box(Modifier.weight(1f, fill = false)) {
                if (visibles.isEmpty()) {
                    Text(
                        stringResource(R.string.sin_resultados),
                        style = MaterialTheme.typography.bodyLarge,
                        color = c.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(RolaSpacing.xl),
                    )
                }
                LazyColumn(state = lista) {
                    items(visibles, key = { it.id }) { song ->
                        val ya = song.id in already
                        val marcada = ya || song.id in marcadas
                        SongRow(
                            song,
                            isCurrent = false,
                            Modifier
                                .toggleable(marcada, enabled = !ya, role = Role.Checkbox) { if (it) marcadas += song.id else marcadas -= song.id }
                                .padding(end = 28.dp), // deja libre el abecedario
                            trailing = { Casilla(marcada) },
                            onClick = null,
                        )
                    }
                }
                if (consulta.isBlank() && visibles.isNotEmpty()) {
                    AlphabetBar(primeras, activa, { i -> scope.launch { lista.scrollToItem(i) } }, Modifier.align(Alignment.CenterEnd))
                }
            }
            val n = marcadas.size
            Button(
                { onAdd(songs.filter { it.id in marcadas }.map { it.id }) },
                Modifier.fillMaxWidth().padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = RolaSpacing.xs, bottom = RolaSpacing.md).height(52.dp),
                enabled = n > 0,
                shape = MaterialTheme.shapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
            ) {
                Text(
                    if (n == 0) stringResource(R.string.agregar_canciones) else pluralStringResource(R.plurals.agregar_n, n, n),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun Buscador(texto: String, onChange: (String) -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        Modifier
            .padding(start = RolaSpacing.lg, end = RolaSpacing.lg, bottom = RolaSpacing.xs)
            .fillMaxWidth()
            .height(52.dp)
            .background(c.surfaceContainer, MaterialTheme.shapes.pill)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_search), null, tint = c.onSurfaceVariant)
        BasicTextField(
            texto,
            onChange,
            Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.onSurface),
            cursorBrush = SolidColor(c.primary),
            decorationBox = { campo ->
                if (texto.isEmpty()) {
                    Text(stringResource(R.string.buscar_en_biblioteca), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
                }
                campo()
            },
        )
    }
}

/** Casilla de Figma 34:1216 / 34:1225: lima con ✓ o solo borde. */
@Composable
private fun Casilla(marcada: Boolean) {
    val c = MaterialTheme.colorScheme
    val forma = MaterialTheme.shapes.extraSmall
    Box(
        Modifier
            .padding(RolaSpacing.sm)
            .size(22.dp)
            .then(if (marcada) Modifier.background(c.primary, forma) else Modifier.border(2.dp, c.onSurfaceVariant, forma)),
        Alignment.Center,
    ) {
        if (marcada) Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp), tint = c.onPrimary)
    }
}
