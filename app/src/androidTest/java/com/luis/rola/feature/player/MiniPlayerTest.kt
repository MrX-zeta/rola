package com.luis.rola.feature.player

import androidx.compose.ui.test.assertIsDisplayed
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
}
