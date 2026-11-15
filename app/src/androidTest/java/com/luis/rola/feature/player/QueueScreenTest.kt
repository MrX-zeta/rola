package com.luis.rola.feature.player

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.media.QueueEntry
import org.junit.Rule
import org.junit.Test

class QueueScreenTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun muestraLaCancionActualYLasQueSiguen() {
        val siguen = listOf(
            QueueEntry(Song(2, "Eco", "Las Nubes", null, 0), 1),
            QueueEntry(Song(3, "Despacio", "Club Sol", null, 0), 2),
        )
        compose.setContent {
            RolaTheme {
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), siguen, onBack = {}, onMove = { _, _ -> }, onRemove = {})
            }
        }
        compose.onNodeWithText("SONANDO AHORA").assertIsDisplayed()
        compose.onNodeWithText("Madrugada").assertIsDisplayed()
        compose.onNodeWithText("A CONTINUACIÓN · 2").assertIsDisplayed()
        compose.onNodeWithText("Eco").assertIsDisplayed()
        compose.onNodeWithText("Despacio").assertIsDisplayed()
    }

    @Test
    fun sinSiguientesLoDice() {
        compose.setContent {
            RolaTheme {
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), emptyList(), onBack = {}, onMove = { _, _ -> }, onRemove = {})
            }
        }
        compose.onNodeWithText("No hay más canciones en la cola.").assertIsDisplayed()
    }
}
