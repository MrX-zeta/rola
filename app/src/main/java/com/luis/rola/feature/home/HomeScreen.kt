package com.luis.rola.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.luis.rola.R
import com.luis.rola.core.data.MostPlayed
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.designsystem.acento
import com.luis.rola.core.designsystem.elevado
import com.luis.rola.core.designsystem.esqueleto
import com.luis.rola.core.designsystem.pill
import com.luis.rola.core.designsystem.timeSmall
import com.luis.rola.feature.library.Cover
import com.luis.rola.feature.library.cascada
import com.luis.rola.feature.library.rememberRelojCascada
import com.luis.rola.feature.onboarding.RolaLogo
import kotlinx.coroutines.delay
import java.time.LocalDateTime

/** Inicio (SCR-10, Figma 30:408), la primera vez (SCR-11, 30:447) y sin canciones (SCR-12, 30:512). Plano salvo SCR-12 (DEC-27). */
@Composable
fun HomeScreen(
    state: HomeState,
    onPlay: (Song) -> Unit,
    onShuffle: () -> Unit,
    onFavorites: () -> Unit,
    onLibrary: () -> Unit,
    onScan: () -> Unit,
    onSettings: () -> Unit,
    scanning: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
    now: LocalDateTime = rememberNow(),
) {
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RolaLogo()
            Spacer(Modifier.weight(1f))
            IconButton(
                onSettings,
                Modifier.padding(end = RolaSpacing.lg).size(48.dp).testTag("ajustes_engrane"),
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Icon(painterResource(R.drawable.ic_settings), stringResource(R.string.ajustes), tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        when (state) {
            HomeState.Loading -> Loading()
            HomeState.NoSongs -> NoSongs(scanning, onScan, onSettings, contentPadding)
            is HomeState.Content -> Content(state, onPlay, onShuffle, onFavorites, onLibrary, contentPadding, now)
        }
    }
}

/** La hora del saludo; se actualiza al volver a la app. */
@Composable
private fun rememberNow(): LocalDateTime {
    var ahora by remember { mutableStateOf(LocalDateTime.now()) }
    LifecycleResumeEffect(Unit) {
        ahora = LocalDateTime.now()
        onPauseOrDispose { }
    }
    return ahora
}

@Composable
private fun Content(
    state: HomeState.Content,
    onPlay: (Song) -> Unit,
    onShuffle: () -> Unit,
    onFavorites: () -> Unit,
    onLibrary: () -> Unit,
    contentPadding: PaddingValues,
    now: LocalDateTime,
) {
    val c = MaterialTheme.colorScheme
    val reloj = rememberRelojCascada(listo = true)
    val fila = { i: Int -> Modifier.cascada(i, { 0 }, reloj) } // orden de lectura (DEC-28)
    val lado = Modifier.padding(horizontal = RolaSpacing.lg)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(contentPadding).padding(top = RolaSpacing.xs, bottom = RolaSpacing.md),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(fila(0).then(lado)) {
            Text(dateText(now), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
            Text(
                stringResource(greeting(now.hour)),
                style = MaterialTheme.typography.displaySmall,
                color = c.onSurface,
                modifier = Modifier.semantics { heading() },
            )
        }
        if (state.recent.isEmpty()) {
            FirstTime(onShuffle, onLibrary, fila(1).then(lado))
        } else {
            Section(R.string.escuchado_hace_poco, fila(1).then(lado))
            LazyRow(
                Modifier.testTag("recientes"),
                contentPadding = PaddingValues(horizontal = RolaSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
            ) {
                itemsIndexed(state.recent, key = { _, s -> s.id }) { i, song -> RecentCard(song, fila(2 + i)) { onPlay(song) } }
            }
        }
        Section(R.string.para_ti, fila(5).then(lado))
        Column(lado, verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) {
            state.mostPlayed?.let { Featured(it, fila(6)) { onPlay(it.song) } }
            Row(fila(7).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) {
                Tile(R.drawable.ic_shuffle, R.string.aleatorio, state.songs, Modifier.weight(1f).fillMaxHeight(), onShuffle)
                Tile(R.drawable.ic_favorite, R.string.favoritas, state.favorites, Modifier.weight(1f).fillMaxHeight(), onFavorites)
            }
        }
    }
}

@Composable
private fun Section(texto: Int, modifier: Modifier) = Text(
    stringResource(texto),
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onSurface,
    modifier = modifier.semantics { heading() },
)

@Composable
private fun RecentCard(song: Song, modifier: Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier
            .width(112.dp)
            .clickable(onClickLabel = stringResource(R.string.reproducir), onClick = onClick)
            .testTag("reciente"),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Cover(song, Modifier.padding(bottom = 6.dp).size(112.dp))
        Text(song.title, style = MaterialTheme.typography.titleSmall, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            song.artist ?: stringResource(R.string.artista_desconocido),
            style = MaterialTheme.typography.bodySmall,
            color = c.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** «Tu más escuchada» (Figma 30:433): todo el mosaico reproduce. */
@Composable
private fun Featured(top: MostPlayed, modifier: Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(c.primaryContainer)
            .clickable(onClickLabel = stringResource(R.string.reproducir), onClick = onClick)
            .padding(start = RolaSpacing.sm, end = RolaSpacing.md, top = RolaSpacing.sm, bottom = RolaSpacing.sm)
            .testTag("mas_escuchada"),
        horizontalArrangement = Arrangement.spacedBy(RolaSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Cover(top.song, Modifier.size(84.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(RolaSpacing.xxs)) {
            Text(stringResource(R.string.tu_mas_escuchada), style = MaterialTheme.typography.timeSmall, color = c.onPrimaryContainer)
            Text(top.song.title, style = MaterialTheme.typography.titleLarge, color = c.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(
                    R.string.separados,
                    top.song.artist ?: stringResource(R.string.artista_desconocido),
                    pluralStringResource(R.plurals.n_veces, top.plays, top.plays),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = c.onSurfaceVariant,
            )
        }
        // Lima en oscuro y negro en claro (Figma 30:439 / 51:2090): en claro el mosaico ya es lima.
        Box(Modifier.size(48.dp).background(c.onPrimaryContainer, CircleShape), Alignment.Center) {
            Icon(painterResource(R.drawable.ic_play), null, tint = c.background)
        }
    }
}

/** Mosaico del bento (Figma 30:442): ícono, nombre y cuántas canciones. */
@Composable
private fun Tile(icono: Int, titulo: Int, n: Int, modifier: Modifier, onClick: () -> Unit) {
    val c = MaterialTheme.colorScheme
    Column(
        modifier
            .heightIn(min = 116.dp)
            .clip(MaterialTheme.shapes.large)
            .background(c.surface)
            .clickable(onClick = onClick)
            .padding(RolaSpacing.md),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.size(40.dp).background(c.primaryContainer, MaterialTheme.shapes.small), Alignment.Center) {
            Icon(painterResource(icono), null, tint = c.onPrimaryContainer)
        }
        Text(
            stringResource(titulo),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = c.onSurface,
        )
        Text(pluralStringResource(R.plurals.n_canciones, n, n), style = MaterialTheme.typography.bodyMedium, color = c.onSurfaceVariant)
    }
}

/** «Aún no escuchas nada» (Figma 30:475). */
@Composable
private fun FirstTime(onShuffle: () -> Unit, onLibrary: () -> Unit, modifier: Modifier) {
    val c = MaterialTheme.colorScheme
    val boton = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    Column(
        modifier.fillMaxWidth().background(c.surfaceContainer, MaterialTheme.shapes.large).padding(RolaSpacing.xl).testTag("primera_vez"),
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
    ) {
        Box(Modifier.size(48.dp).background(c.primaryContainer, CircleShape), Alignment.Center) {
            Icon(painterResource(R.drawable.ic_music_note), null, tint = c.onPrimaryContainer)
        }
        Text(stringResource(R.string.primera_vez_titulo), style = MaterialTheme.typography.titleLarge, color = c.onSurface)
        Text(stringResource(R.string.primera_vez_texto), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant)
        FlowRow(Modifier.padding(top = RolaSpacing.xs), Arrangement.spacedBy(10.dp), Arrangement.spacedBy(RolaSpacing.xs)) {
            Button(
                onShuffle,
                Modifier.height(48.dp),
                shape = MaterialTheme.shapes.pill,
                colors = ButtonDefaults.buttonColors(containerColor = RolaTheme.colors.accent, contentColor = RolaTheme.colors.onAccent),
            ) {
                Icon(painterResource(R.drawable.ic_shuffle), null, Modifier.size(20.dp))
                Text(stringResource(R.string.aleatorio), style = boton, modifier = Modifier.padding(start = RolaSpacing.xs))
            }
            TextButton(onLibrary, Modifier.height(48.dp)) { Text(stringResource(R.string.ir_a_biblioteca), style = boton, color = c.onSurface) }
        }
    }
}

/** «No encontramos música» (SCR-12): relieve en el círculo y el botón (DEC-27). */
@Composable
private fun NoSongs(scanning: Boolean, onScan: () -> Unit, onSettings: () -> Unit, contentPadding: PaddingValues) {
    val c = MaterialTheme.colorScheme
    val acento = RolaTheme.colors.accent
    val sobreAcento = RolaTheme.colors.onAccent
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = RolaSpacing.xxl, vertical = RolaSpacing.xl)
            .testTag("sin_canciones"),
        verticalArrangement = Arrangement.spacedBy(RolaSpacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(112.dp).elevado(CircleShape).background(c.surfaceContainer, CircleShape), Alignment.Center) {
            Icon(painterResource(R.drawable.ic_music_note), null, Modifier.size(48.dp), tint = c.onSurfaceVariant)
        }
        Spacer(Modifier.size(RolaSpacing.xxs))
        Text(
            stringResource(R.string.sin_musica_titulo),
            style = MaterialTheme.typography.headlineSmall,
            color = c.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.sin_musica_texto), style = MaterialTheme.typography.bodyLarge, color = c.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.size(RolaSpacing.xxs))
        Button(
            onScan,
            Modifier.heightIn(min = 52.dp).acento(MaterialTheme.shapes.pill),
            enabled = !scanning,
            shape = MaterialTheme.shapes.pill,
            // Escaneando sigue lima: el progreso ya dice que está ocupado.
            colors = ButtonDefaults.buttonColors(acento, sobreAcento, acento, sobreAcento),
        ) {
            if (scanning) {
                CircularProgressIndicator(Modifier.size(20.dp), color = sobreAcento, strokeWidth = 2.dp)
            } else {
                Icon(painterResource(R.drawable.ic_refresh), null, Modifier.size(20.dp))
            }
            Text(
                stringResource(if (scanning) R.string.escaneando else R.string.escanear_de_nuevo),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(start = RolaSpacing.xs),
            )
        }
        TextButton(onSettings, Modifier.height(52.dp).testTag("abrir_ajustes")) {
            Text(stringResource(R.string.abrir_ajustes), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = c.onSurface)
        }
    }
}

/** Esqueletos con la forma de Inicio; solo si la carga pasa de ~300 ms (DEC-09). */
@Composable
private fun Loading() {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300)
        visible = true
    }
    if (!visible) return
    val descripcion = stringResource(R.string.cargando_canciones)
    val s = MaterialTheme.shapes
    Column(
        Modifier.padding(horizontal = RolaSpacing.lg, vertical = RolaSpacing.xs).semantics { contentDescription = descripcion },
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(Modifier.size(width = 240.dp, height = 60.dp).esqueleto(s.small))
        Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) { repeat(3) { Box(Modifier.weight(1f).height(112.dp).esqueleto(s.small)) } }
        Box(Modifier.fillMaxWidth().height(108.dp).esqueleto(s.large))
        Row(horizontalArrangement = Arrangement.spacedBy(RolaSpacing.sm)) { repeat(2) { Box(Modifier.weight(1f).height(116.dp).esqueleto(s.large)) } }
    }
}
