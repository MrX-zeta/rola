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
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import androidx.compose.ui.geometry.Offset
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

    private val larga = (0 until 40).map { QueueEntry(Song(100L + it, "Canción $it", "Ana Vela", null, 0), it + 1) }

    /** Arrastra el asa de la fila [fila] [dy] dp, la sostiene [sostener] ms y suelta; devuelve la orden de mover. */
    private fun arrastrar(fila: Int, dy: Float, sostener: Long, alSostener: () -> Unit = {}): Pair<Int, Int>? {
        var orden: Pair<Int, Int>? = null
        compose.setContent {
            RolaTheme {
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), larga, onBack = {}, onMove = { a, b -> orden = a to b }, onRemove = {}, onPlay = {}, onClear = {})
            }
        }
        val asa = compose.onAllNodesWithContentDescription("Arrastrar para mover", useUnmergedTree = true)[fila]
        // Con el dedo abajo el autodesplazamiento pide cuadros sin parar: el reloj se avanza a mano o la prueba no termina.
        compose.mainClock.autoAdvance = false
        asa.performTouchInput { down(center) }
        // Pasos cortos, un cuadro cada uno: cada evento del dedo cruza a lo sumo una fila, como en el teléfono.
        repeat(40) {
            asa.performTouchInput { moveBy(Offset(0f, dy.dp.toPx() / 40)) }
            compose.mainClock.advanceTimeByFrame()
        }
        compose.mainClock.advanceTimeBy(sostener)
        alSostener()
        asa.performTouchInput { up() }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
        return orden
    }

    @Test
    fun arrastradaAlPrimerLugarQuedaEnIndiceCeroDentroDeLaLista() {
        val orden = arrastrar(fila = 3, dy = -900f, sostener = 500) {
            val lista = compose.onNodeWithTag("cola").getUnclippedBoundsInRoot()
            val fila = compose.onNodeWithText("Canción 3").getUnclippedBoundsInRoot()
            assertTrue("fila ${fila.top} fuera de ${lista.top}", fila.top >= lista.top)
        }
        assertEquals(3 to 0, orden)
    }

    @Test
    fun arrastradaMasAllaDelFinalQuedaEnElUltimoIndice() {
        // El dedo se queda abajo: la lista se desplaza sola hasta el final y se detiene; la fila no sale de la vista.
        val orden = arrastrar(fila = 0, dy = 2_000f, sostener = 10_000) {
            val lista = compose.onNodeWithTag("cola").getUnclippedBoundsInRoot()
            val fila = compose.onNodeWithText("Canción 0").getUnclippedBoundsInRoot()
            assertTrue("fila ${fila.bottom} fuera de ${lista.bottom}", fila.bottom <= lista.bottom)
        }
        assertEquals(0 to 39, orden)
    }
}
