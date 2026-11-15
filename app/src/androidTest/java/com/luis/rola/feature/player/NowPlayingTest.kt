package com.luis.rola.feature.player

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.media.PlayerState
import com.luis.rola.core.media.Repeat
import org.junit.Rule
import org.junit.Test

/** Colores del tema, sin portada: las pruebas no dependen de Coil ni de Palette. */
@Composable
fun coloresDePrueba() = CoverColors(
    container = MaterialTheme.colorScheme.surfaceContainer,
    accent = RolaTheme.colors.accent,
    onAccent = RolaTheme.colors.onAccent,
    content = MaterialTheme.colorScheme.onSurface,
    contentVariant = MaterialTheme.colorScheme.onSurfaceVariant,
    track = MaterialTheme.colorScheme.outline,
    play = RolaTheme.colors.accent,
    onPlay = RolaTheme.colors.onAccent,
)

class NowPlayingTest {

    @get:Rule
    val compose = createComposeRule()

    private val cancion = Song(1, "Madrugada", "Ana Vela", null, 0)

    @Test
    fun muestraTituloTiemposYModos() {
        var estado by mutableStateOf(
            PlayerState(current = cancion, positionMs = 84_000, durationMs = 222_000, shuffle = true, repeat = Repeat.ONE),
        )
        compose.setContent {
            RolaTheme {
                val posicion = remember { mutableLongStateOf(84_000) }
                NowPlayingScreen(
                    estado, coloresDePrueba(), posicion,
                    onClose = {}, onToggle = {}, onPrevious = {}, onNext = {}, onSeek = {},
                    onShuffle = {}, onRepeat = {}, onOpenQueue = {}, onOpenTimer = {},
                )
            }
        }
        compose.onNodeWithText("Madrugada").assertIsDisplayed()
        compose.onNodeWithText("Ana Vela").assertIsDisplayed()
        compose.onNodeWithText("1:24").assertIsDisplayed()
        compose.onNodeWithText("3:42").assertIsDisplayed()
        compose.onNodeWithContentDescription("Aleatorio encendido").assertIsDisplayed()
        compose.onNodeWithContentDescription("Repetir una canción").assertIsDisplayed()

        estado = estado.copy(shuffle = false, repeat = Repeat.ALL)
        compose.onNodeWithContentDescription("Aleatorio apagado").assertIsDisplayed()
        compose.onNodeWithContentDescription("Repetir toda la cola").assertIsDisplayed()

        estado = estado.copy(repeat = Repeat.OFF)
        compose.onNodeWithContentDescription("Repetir apagado").assertIsDisplayed()
    }
}
