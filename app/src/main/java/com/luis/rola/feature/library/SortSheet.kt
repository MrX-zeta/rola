package com.luis.rola.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.SongSort
import com.luis.rola.core.data.SortBy
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill

/** Nombre del criterio («Título», «Artista», «Álbum»). */
internal fun SortBy.label() = when (this) {
    SortBy.TITLE -> R.string.orden_titulo
    SortBy.ARTIST -> R.string.orden_artista
    SortBy.ALBUM -> R.string.orden_album
}

/** «Título A–Z» para el resumen de Canciones. */
@Composable
internal fun sortSummary(sort: SongSort) =
    stringResource(if (sort.ascending) R.string.orden_resumen_az else R.string.orden_resumen_za, stringResource(sort.by.label()))

/**
 * Hoja de filtros y orden (SCR-26, Figma 33:1070): criterio con opciones de radio y dirección con un selector
 * segmentado. Cada toque se aplica al momento (la lista de atrás se reordena); «Listo» solo cierra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(sort: SongSort, onChange: (SongSort) -> Unit, onDismiss: () -> Unit) {
    val c = MaterialTheme.colorScheme
    val etiqueta = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
    ModalBottomSheet(
        onDismiss,
        Modifier.testTag("hoja_orden"),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = c.surface,
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = RolaSpacing.xl)) {
            Text(
                stringResource(R.string.ordenar_y_filtrar),
                style = MaterialTheme.typography.titleLarge,
                color = c.onSurface,
                modifier = Modifier.padding(start = RolaSpacing.xl, end = RolaSpacing.xl, bottom = RolaSpacing.xs),
            )
            Text(
                stringResource(R.string.ordenar_por),
                style = etiqueta,
                color = c.onSurfaceVariant,
                modifier = Modifier.padding(start = RolaSpacing.xl, top = RolaSpacing.xs, bottom = RolaSpacing.xxs),
            )
            Column(Modifier.selectableGroup()) {
                SortBy.entries.forEach { by ->
                    val elegido = sort.by == by
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(elegido, role = Role.RadioButton) { onChange(sort.copy(by = by)) }
                            .padding(horizontal = RolaSpacing.xl, vertical = RolaSpacing.xxs),
                        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(elegido, null, colors = RadioButtonDefaults.colors(selectedColor = RolaTheme.colors.accent))
                        Text(
                            stringResource(by.label()),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = c.onSurface,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.direccion),
                style = etiqueta,
                color = c.onSurfaceVariant,
                modifier = Modifier.padding(start = RolaSpacing.xl, top = RolaSpacing.xs, bottom = RolaSpacing.xxs),
            )
            // Selector segmentado: fondo surfaceContainer, el elegido en lima.
            Row(
                Modifier
                    .padding(horizontal = RolaSpacing.xl, vertical = RolaSpacing.xxs)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.pill)
                    .background(c.surfaceContainer)
                    .padding(RolaSpacing.xxs)
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(RolaSpacing.xxs),
            ) {
                listOf(true to R.string.orden_az, false to R.string.orden_za).forEach { (asc, texto) ->
                    val elegido = sort.ascending == asc
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(MaterialTheme.shapes.pill)
                            .then(if (elegido) Modifier.background(RolaTheme.colors.accent) else Modifier)
                            .selectable(elegido, role = Role.RadioButton) { onChange(sort.copy(ascending = asc)) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(texto),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (elegido) RolaTheme.colors.onAccent else c.onSurfaceVariant,
                        )
                    }
                }
            }
            Button(
                onDismiss,
                Modifier.fillMaxWidth().padding(start = RolaSpacing.xl, end = RolaSpacing.xl, top = RolaSpacing.md).height(52.dp),
                shape = MaterialTheme.shapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
            ) {
                Text(stringResource(R.string.listo), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        }
    }
}
