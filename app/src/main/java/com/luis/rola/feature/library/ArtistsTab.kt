package com.luis.rola.feature.library

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luis.rola.R
import com.luis.rola.core.data.Artist
import com.luis.rola.core.designsystem.RolaSpacing

/** Biblioteca · Artistas (SCR-22, Figma 32:961): foto redonda, nombre, conteo y chevron; por nombre. */
@Composable
fun ArtistsTab(
    artists: List<Artist>,
    state: LazyListState,
    onOpen: (String?) -> Unit,
    primera: () -> Int,
    reloj: Animatable<Float, AnimationVector1D>,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val c = MaterialTheme.colorScheme
    LazyColumn(Modifier.fillMaxSize(), state = state, contentPadding = contentPadding) {
        itemsIndexed(artists, key = { _, a -> a.name ?: "" }) { i, artist ->
            Row(
                Modifier
                    .cascada(i, primera, reloj)
                    .fillMaxWidth()
                    .clickable { onOpen(artist.name) }
                    .padding(start = RolaSpacing.lg, end = 30.dp, top = RolaSpacing.xs, bottom = RolaSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                artist.cover?.let { Cover(it, Modifier.size(56.dp), CircleShape) }
                Column(Modifier.weight(1f)) {
                    Text(
                        artist.name ?: stringResource(R.string.artista_desconocido),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = c.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(artistCounts(artist), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
                }
                Icon(painterResource(R.drawable.ic_chevron_forward), null, Modifier.padding(RolaSpacing.sm), tint = c.onSurfaceVariant)
            }
        }
    }
}
