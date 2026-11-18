package com.luis.rola.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalResources
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.media.PlayerConnection
import com.luis.rola.core.media.PlayerState
import com.luis.rola.core.media.QueueState
import com.luis.rola.core.media.SleepTimerOption
import com.luis.rola.core.media.formatDuration
import com.luis.rola.feature.player.MiniCardContent
import com.luis.rola.feature.player.NowPlayingScreen
import com.luis.rola.feature.player.PlayerSheet
import com.luis.rola.feature.player.PlayerSheetState
import com.luis.rola.feature.player.QueueScreen
import com.luis.rola.feature.player.SleepTimerSheet
import com.luis.rola.feature.player.rememberCoverColors
import com.luis.rola.feature.player.rememberPlayerSheetState
import com.luis.rola.feature.player.rememberPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.clearAndSetSemantics
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

    val queue by player.queue.collectAsStateWithLifecycle()
    val sheet = rememberPlayerSheetState()
    // Sin canción no hay Reproduciendo: se cierra sin animar (p. ej. la canción dejó de existir).
    LaunchedEffect(playerState.current == null) {
        if (playerState.current == null) {
            sheet.expansion.snapTo(0f)
            sheet.queueOpen = false
        }
    }

    val snackbar = remember { SnackbarHostState() }
    // Los avisos viven aquí y no en la capa: cerrar Reproduciendo no debe llevarse el «Deshacer».
    val avisos = rememberCoroutineScope()
    val errorTexto = stringResource(R.string.error_reproducir)
    LaunchedEffect(Unit) { player.errors.collect { snackbar.showSnackbar(errorTexto) } }

    var abajoPx by remember { mutableIntStateOf(0) }
    val abajo = with(LocalDensity.current) { abajoPx.toDp() }
    val fondo = MaterialTheme.colorScheme.background

    Box(Modifier.fillMaxSize().background(fondo)) {
        // Biblioteca vive fuera del NavHost y no se desmonta al cambiar de pestaña: desmontarla y rearmarla
        // ocupaba el hilo principal a media animación de la píldora (medido en T02d). Oculta sigue medida y
        // colocada con alfa 0, para que volver no rehaga sus capas (T02e); sin semántica y sin toques.
        // Su destino en el NavHost lleva la pila.
        val vm: LibraryViewModel = viewModel(factory = LibraryViewModel.factory(container.songs, container.library))
        val songs by vm.songs.collectAsStateWithLifecycle()
        // El mismo fundido que el NavHost da a las demás pestañas.
        val alfa = remember { Animatable(if (enBiblioteca) 1f else 0f) }
        LaunchedEffect(enBiblioteca) {
            if (enBiblioteca) {
                withFrameNanos {} // DEC-28 arranque_en_primer_cuadro: cuenta desde que Biblioteca ya está dibujada
                alfa.animateTo(1f, tween(RolaMotion.MEDIA, easing = RolaMotion.EaseOut))
            } else {
                alfa.animateTo(0f, tween(RolaMotion.RAPIDA))
            }
        }
        Box(
            Modifier
                .graphicsLayer { alpha = alfa.value }
                .then(
                    if (enBiblioteca) {
                        Modifier
                    } else {
                        // Oculta: ni lector de pantalla ni foco de teclado entran a sus filas.
                        Modifier.clearAndSetSemantics {}.focusProperties { onEnter = { cancelFocusChange() } }.focusGroup()
                    },
                ),
        ) {
            LibraryScreen(
                songs = songs,
                currentId = playerState.current?.id,
                onSongClick = { i -> songs?.let { player.play(it, i) } },
                onShuffle = { songs?.let { player.shuffle(it) } },
                contentPadding = PaddingValues(bottom = abajo),
                visible = enBiblioteca,
            )
            // Encima de la lista oculta: se lleva los toques para que no lleguen a las filas.
            if (!enBiblioteca) Box(Modifier.matchParentSize().pointerInput(Unit) {})
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
            MiniPlayer(playerState, player::togglePlayPause, player::next, onPrevious = player::previous, sheet = sheet)
            RolaNavBar(ruta, onSelect = { d ->
                nav.navigate(d.ruta) {
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        val song = playerState.current
        if (sheet.visible && song != null) PlayerOverlay(sheet, playerState, song, queue, player, snackbar, avisos)
        // Encima de la capa del reproductor: el aviso de Deshacer de la cola también se ve (DEC-09).
        SnackbarHost(
            snackbar,
            Modifier.align(Alignment.BottomCenter).then(
                if (sheet.visible) Modifier.navigationBarsPadding().padding(bottom = RolaSpacing.md) else Modifier.padding(bottom = abajo),
            ),
        ) {
            // Colores de Figma (SCR-52): acción en primaryContainer.
            Snackbar(it, shape = MaterialTheme.shapes.small, actionColor = MaterialTheme.colorScheme.primaryContainer)
        }
    }
}

/** Reproduciendo con Cola y la hoja del temporizador, dentro de la capa que crece desde la tarjeta. */
@Composable
private fun PlayerOverlay(
    sheet: PlayerSheetState,
    state: PlayerState,
    song: Song,
    queue: QueueState,
    player: PlayerConnection,
    snackbar: SnackbarHostState,
    avisos: CoroutineScope,
) {
    val reduce = LocalReduceMotion.current
    val scope = rememberCoroutineScope()
    val res = LocalResources.current
    val colores = rememberCoverColors(song)
    val posicion = rememberPosition(state)
    var temporizador by rememberSaveable { mutableStateOf(false) }
    fun aviso(texto: String, accion: String? = null, alAccion: () -> Unit = {}) {
        avisos.launch {
            snackbar.currentSnackbarData?.dismiss() // el aviso nuevo reemplaza al anterior
            if (snackbar.showSnackbar(texto, accion, duration = SnackbarDuration.Short) == SnackbarResult.ActionPerformed) alAccion()
        }
    }
    PlayerSheet(
        sheet,
        colores.container,
        card = { MiniCardContent(state, song, colores, posicion, onToggle = {}) },
    ) {
        NowPlayingScreen(
            state,
            colores,
            posicion,
            onClose = { scope.launch { sheet.close(reduce) } },
            onToggle = player::togglePlayPause,
            onPrevious = player::previous,
            onNext = player::next,
            onSeek = player::seekTo,
            onShuffle = player::toggleShuffle,
            onRepeat = player::cycleRepeat,
            onOpenQueue = { sheet.queueOpen = true },
            onOpenTimer = { temporizador = true },
        )
        AnimatedVisibility(
            sheet.queueOpen,
            enter = if (reduce) {
                fadeIn(tween(RolaMotion.MEDIA))
            } else {
                slideInVertically(tween(RolaMotion.LENTA, easing = RolaMotion.EaseOut)) { it } + fadeIn(tween(RolaMotion.MEDIA))
            },
            exit = if (reduce) {
                fadeOut(tween(RolaMotion.RAPIDA))
            } else {
                slideOutVertically(tween(200, easing = RolaMotion.EaseOut)) { it } + fadeOut(tween(RolaMotion.RAPIDA))
            },
        ) {
            BackHandler { sheet.queueOpen = false }
            QueueScreen(
                song,
                colores,
                queue.upNext,
                onBack = { sheet.queueOpen = false },
                onMove = player::moveUpNext,
                onRemove = { i ->
                    player.removeUpNext(i)?.let { r ->
                        aviso(res.getString(R.string.quitaste, r.item.mediaMetadata.title), res.getString(R.string.deshacer)) {
                            player.undoRemove(r)
                        }
                    }
                },
                onPlay = player::playUpNext,
                onClear = {
                    player.clearUpNext()?.let { c ->
                        aviso(res.getString(R.string.limpiaste), res.getString(R.string.deshacer)) { player.undoClear(c) }
                    }
                },
            )
        }
        if (temporizador) {
            SleepTimerSheet(
                state.timer,
                state.timerEndsAtMs,
                onStart = { o ->
                    player.setSleepTimer(o)
                    temporizador = false
                    aviso(
                        when (o) {
                            SleepTimerOption.OFF -> res.getString(R.string.temporizador_cancelado)
                            SleepTimerOption.END_OF_SONG -> res.getString(R.string.temporizador_restante_cancion)
                            else -> res.getString(R.string.temporizador_restante, formatDuration(o.minutes * 60_000L))
                        },
                    )
                },
                onDismiss = { temporizador = false },
            )
        }
    }
}

/** «Próximamente» de Inicio y Buscar mientras llegan sus tareas. */
@Composable
fun ComingSoonScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(stringResource(R.string.proximamente), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
