package com.luis.rola.feature.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
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
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), siguen, onBack = {}, onMove = { _, _ -> }, onRemove = {}, onPlay = {}, onClear = {})
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
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), emptyList(), onBack = {}, onMove = { _, _ -> }, onRemove = {}, onPlay = {}, onClear = {})
            }
        }
        compose.onNodeWithText("No hay más canciones en la cola.").assertIsDisplayed()
        compose.onNodeWithText("Limpiar").assertIsNotEnabled()
    }

    private val siguen = listOf(
        QueueEntry(Song(2, "Eco", "Las Nubes", null, 0), 1),
        QueueEntry(Song(3, "Despacio", "Club Sol", null, 0), 2),
    )

    @Test
    fun tocarUnaFilaLaReproduce() {
        var tocada = -1
        compose.setContent {
            RolaTheme {
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), siguen, onBack = {}, onMove = { _, _ -> }, onRemove = {}, onPlay = { tocada = it }, onClear = {})
            }
        }
        compose.onNodeWithText("Despacio").performClick()
        assertEquals(1, tocada)
    }

    @Test
    fun limpiarYDeshacerDevuelveLasCanciones() {
        // La cola real vive en el reproductor: aquí «Limpiar» la vacía y «Deshacer» la regresa, como hace RolaNavHost.
        var cola by mutableStateOf(siguen)
        compose.setContent {
            RolaTheme {
                QueueScreen(
                    Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), cola,
                    onBack = {}, onMove = { _, _ -> }, onRemove = {}, onPlay = {}, onClear = { cola = emptyList() },
                )
            }
        }
        compose.onNodeWithText("Limpiar").performClick()
        compose.onNodeWithText("A CONTINUACIÓN · 0").assertIsDisplayed()
        compose.onNodeWithText("Madrugada").assertIsDisplayed() // la actual se queda
        cola = siguen // «Deshacer»
        compose.onNodeWithText("A CONTINUACIÓN · 2").assertIsDisplayed()
        compose.onNodeWithText("Eco").assertIsDisplayed()
    }
}
