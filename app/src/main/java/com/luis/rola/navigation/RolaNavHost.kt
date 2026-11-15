package com.luis.rola.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.luis.rola.AppContainer
import com.luis.rola.R
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.feature.library.LibraryScreen
import com.luis.rola.feature.library.LibraryViewModel
import com.luis.rola.feature.player.MiniPlayer

/** Pestañas (DEC-03) con la mini tarjeta y la píldora abajo. Sin deslizar; fundido corto entre pestañas (DEC-28). */
@Composable
fun RolaNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val player = container.player
    val playerState by player.state.collectAsStateWithLifecycle()
    // Antes del primer destino la ruta es null: se asume el de arranque para no animar al abrir.
    val ruta = nav.currentBackStackEntryAsState().value?.destination?.route ?: Destino.Biblioteca.ruta
    val enBiblioteca = ruta == Destino.Biblioteca.ruta

    val snackbar = remember { SnackbarHostState() }
    val errorTexto = stringResource(R.string.error_reproducir)
    LaunchedEffect(Unit) { player.errors.collect { snackbar.showSnackbar(errorTexto) } }

    var abajoPx by remember { mutableIntStateOf(0) }
    val abajo = with(LocalDensity.current) { abajoPx.toDp() }
    val fondo = MaterialTheme.colorScheme.background

    Box(Modifier.fillMaxSize().background(fondo)) {
        // Biblioteca vive fuera del NavHost y no se desmonta al cambiar de pestaña: desmontarla y rearmarla
        // ocupaba el hilo principal a media animación de la píldora (medido en T02d). Oculta, no se coloca:
        // no se dibuja, no recibe toques ni aparece en accesibilidad. Su destino en el NavHost lleva la pila.
        val vm: LibraryViewModel = viewModel(factory = LibraryViewModel.factory(container.songs))
        val songs by vm.songs.collectAsStateWithLifecycle()
        // El mismo fundido que el NavHost da a las demás pestañas.
        val alfa by animateFloatAsState(
            if (enBiblioteca) 1f else 0f,
            if (enBiblioteca) tween(RolaMotion.MEDIA, easing = RolaMotion.EaseOut) else tween(RolaMotion.RAPIDA),
            label = "biblioteca",
        )
        Box(
            Modifier.layout { m, c ->
                val p = m.measure(c)
                layout(p.width, p.height) { if (alfa > 0f) p.placeWithLayer(0, 0) { alpha = alfa } }
            },
        ) {
            LibraryScreen(
                songs = songs,
                currentId = playerState.current?.id,
                onSongClick = { i -> songs?.let { player.play(it, i) } },
                onShuffle = { songs?.let { player.shuffle(it) } },
                contentPadding = PaddingValues(bottom = abajo),
            )
        }
        NavHost(
            nav,
            startDestination = Destino.Biblioteca.ruta, // DEC-03 temporal: Inicio todavía no existe
            // Fundido corto entre pestañas hermanas, sin desplazamiento (DEC-28); válido también con reducir movimiento.
            enterTransition = { fadeIn(tween(RolaMotion.MEDIA, easing = RolaMotion.EaseOut)) },
            exitTransition = { fadeOut(tween(RolaMotion.RAPIDA)) },
        ) {
            composable(Destino.Inicio.ruta) { ComingSoonScreen() }
            composable(Destino.Biblioteca.ruta) {} // se dibuja arriba, fuera del NavHost
            composable(Destino.Buscar.ruta) { ComingSoonScreen() }
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(0f to Color.Transparent, 0.45f to fondo.copy(alpha = 0.92f), 1f to fondo))
                .navigationBarsPadding()
                .padding(start = RolaSpacing.md, end = RolaSpacing.md, top = RolaSpacing.xs, bottom = RolaSpacing.sm)
                .onSizeChanged { abajoPx = it.height },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MiniPlayer(playerState, player::togglePlayPause, player::next)
            RolaNavBar(ruta, onSelect = { d ->
                nav.navigate(d.ruta) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = abajo))
    }
}

/** «Próximamente» de Inicio y Buscar mientras llegan sus tareas. */
@Composable
fun ComingSoonScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(stringResource(R.string.proximamente), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
