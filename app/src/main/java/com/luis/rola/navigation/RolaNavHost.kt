package com.luis.rola.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import com.luis.rola.core.designsystem.RolaSpacing
import com.luis.rola.feature.library.LibraryScreen
import com.luis.rola.feature.library.LibraryViewModel
import com.luis.rola.feature.player.MiniPlayer

/** Pestañas (DEC-03) con la mini tarjeta y la píldora abajo. Sin deslizar y sin animaciones entre pestañas. */
@Composable
fun RolaNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val player = container.player
    val playerState by player.state.collectAsStateWithLifecycle()
    val ruta = nav.currentBackStackEntryAsState().value?.destination?.route

    val snackbar = remember { SnackbarHostState() }
    val errorTexto = stringResource(R.string.error_reproducir)
    LaunchedEffect(Unit) { player.errors.collect { snackbar.showSnackbar(errorTexto) } }

    var abajoPx by remember { mutableIntStateOf(0) }
    val abajo = with(LocalDensity.current) { abajoPx.toDp() }
    val fondo = MaterialTheme.colorScheme.background

    Box(Modifier.fillMaxSize().background(fondo)) {
        NavHost(
            nav,
            startDestination = Destino.Biblioteca.ruta, // DEC-03 temporal: Inicio todavía no existe
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable(Destino.Inicio.ruta) { ComingSoonScreen() }
            composable(Destino.Biblioteca.ruta) {
                val vm: LibraryViewModel = viewModel(factory = LibraryViewModel.factory(container.songs))
                val songs by vm.songs.collectAsStateWithLifecycle()
                LibraryScreen(
                    songs = songs,
                    currentId = playerState.current?.id,
                    onSongClick = { i -> songs?.let { player.play(it, i) } },
                    onShuffle = { songs?.let { player.shuffle(it) } },
                    contentPadding = PaddingValues(bottom = abajo),
                )
            }
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
            verticalArrangement = Arrangement.spacedBy(RolaSpacing.sm),
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
