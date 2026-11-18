package com.luis.rola.feature.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.PlaylistDetail
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.pill
import com.luis.rola.feature.library.MenuPlace
import com.luis.rola.feature.library.SongAction
import com.luis.rola.feature.library.SongItem

/**
 * Lista vacía (SCR-31) o con canciones (SCR-32); también Favoritas ([isFavorites]: sin Agregar, otro
 * vacío). Sin ⋮ de la lista: renombrar y borrar quedan fuera de esta tarea.
 */
@Composable
fun PlaylistScreen(
    detail: PlaylistDetail,
    isFavorites: Boolean,
    currentId: Long?,
    onBack: () -> Unit,
    onPlay: (index: Int) -> Unit,
    onShuffle: () -> Unit,
    onAdd: () -> Unit,
    onSongAction: (Song, SongAction) -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val c = MaterialTheme.colorScheme
    val nombre = if (isFavorites) stringResource(R.string.favoritas) else detail.name
    val minutos = (detail.totalMs / 60_000).toInt()
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = contentPadding) {
        item {
            Row(Modifier.height(64.dp).padding(horizontal = RolaSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onBack) { Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.volver), tint = c.onSurface) }
            }
        }
        item {
            Row(
                Modifier.padding(start = RolaSpacing.lg, end = RolaSpacing.lg, top = RolaSpacing.xxs, bottom = RolaSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlaylistCover(detail.songs.take(4), 112.dp, MaterialTheme.shapes.large, emptyIcon = true)
                Column(verticalArrangement = Arrangement.spacedBy(RolaSpacing.xxs)) {
                    Text(nombre, style = MaterialTheme.typography.headlineMedium, color = c.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        pluralStringResource(R.plurals.resumen_lista, detail.songs.size, detail.songs.size) +
                            if (minutos > 0) " · " + stringResource(R.string.minutos, minutos) else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = c.onSurfaceVariant,
                    )
                }
            }
        }
        if (detail.songs.isEmpty()) {
            item { Empty(isFavorites, onAdd) }
            return@LazyColumn
        }
        item {
            Row(
                Modifier.padding(start = RolaSpacing.lg, end = RolaSpacing.lg, bottom = RolaSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    { onPlay(0) },
                    Modifier.height(48.dp),
                    shape = MaterialTheme.shapes.pill,
                    colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
                ) {
                    Icon(painterResource(R.drawable.ic_play), null, Modifier.size(20.dp))
                    Text(
                        stringResource(R.string.reproducir),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = RolaSpacing.xs),
                    )
                }
                Circle(R.drawable.ic_shuffle, R.string.aleatorio, onShuffle)
                if (!isFavorites) Circle(R.drawable.ic_add, R.string.agregar, onAdd)
            }
        }
        itemsIndexed(detail.songs, key = { _, s -> s.id }) { i, song ->
            SongItem(song, song.id == currentId, MenuPlace.Playlist, favorite = false, onAction = { onSongAction(song, it) }) { onPlay(i) }
        }
    }
}

@Composable
private fun Circle(icon: Int, label: Int, onClick: () -> Unit) = FilledIconButton(
    onClick,
    Modifier.size(48.dp),
    colors = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
) { Icon(painterResource(icon), stringResource(label)) }

@Composable
private fun Empty(isFavorites: Boolean, onAdd: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(start = RolaSpacing.xxl, end = RolaSpacing.xxl, top = RolaSpacing.xl).testTag("lista_vacia"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
    ) {
        Text(
            stringResource(if (isFavorites) R.string.favoritas_vacia_titulo else R.string.lista_vacia_titulo),
            style = MaterialTheme.typography.titleLarge,
            color = c.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(if (isFavorites) R.string.favoritas_vacia_texto else R.string.lista_vacia_texto),
            style = MaterialTheme.typography.bodyLarge,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (isFavorites) return@Column
        Button(
            onAdd,
            Modifier.padding(top = RolaSpacing.xs).height(52.dp),
            shape = MaterialTheme.shapes.pill,
            colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
        ) {
            Icon(painterResource(R.drawable.ic_add), null, Modifier.size(20.dp))
            Text(
                stringResource(R.string.agregar_canciones),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = RolaSpacing.xs),
            )
        }
    }
}
