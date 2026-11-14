package com.luis.rola.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.unit.dp
import com.luis.rola.core.designsystem.LocalReduceMotion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.media.PlayerState
import org.junit.Rule
import org.junit.Test

class MiniPlayerTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sinCancionNoAparece() {
        compose.setContent { RolaTheme { MiniPlayer(PlayerState(), onToggle = {}, onNext = {}) } }
        compose.onNodeWithContentDescription("Reproducir").assertDoesNotExist()
        compose.onNodeWithContentDescription("Siguiente").assertDoesNotExist()
    }

    @Test
    fun conCancionMuestraTituloYControles() {
        val estado = PlayerState(current = Song(1, "Madrugada", "Ana Vela", null, 0), isPlaying = true, hasNext = true)
        compose.setContent { RolaTheme { MiniPlayer(estado, onToggle = {}, onNext = {}) } }
        compose.onNodeWithText("Madrugada").assertIsDisplayed()
        compose.onNodeWithText("Ana Vela").assertIsDisplayed()
        compose.onNodeWithContentDescription("Pausar").assertIsDisplayed()
        compose.onNodeWithContentDescription("Siguiente").assertIsDisplayed()
    }

    /** Posición del título 32 ms después de cargar la canción y al terminar la entrada. */
    private fun posicionesAlEntrar(reduce: Boolean): Pair<Float, Float> {
        var estado by mutableStateOf(PlayerState())
        compose.mainClock.autoAdvance = false
        compose.setContent {
            RolaTheme {
                CompositionLocalProvider(LocalReduceMotion provides reduce) {
                    Box(Modifier.size(400.dp, 200.dp), contentAlignment = Alignment.BottomCenter) {
                        MiniPlayer(estado, onToggle = {}, onNext = {})
                    }
                }
            }
        }
        estado = PlayerState(current = Song(1, "Madrugada", "Ana Vela", null, 0))
        compose.mainClock.advanceTimeBy(32)
        val alInicio = compose.onNodeWithText("Madrugada").getUnclippedBoundsInRoot().top.value
        compose.mainClock.advanceTimeBy(1_000)
        val alFinal = compose.onNodeWithText("Madrugada").getUnclippedBoundsInRoot().top.value
        return alInicio to alFinal
    }

    @Test
    fun conReducirMovimientoNoSeDesplaza() {
        val (alInicio, alFinal) = posicionesAlEntrar(reduce = true)
        assertEquals(alFinal, alInicio, 0.5f)
    }

    @Test
    fun sinReducirMovimientoEntraDesdeAbajo() {
        val (alInicio, alFinal) = posicionesAlEntrar(reduce = false)
        assertTrue("debería venir de más abajo: $alInicio vs $alFinal", alInicio > alFinal + 1f)
    }
}
