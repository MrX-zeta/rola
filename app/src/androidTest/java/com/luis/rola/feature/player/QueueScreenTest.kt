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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
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
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), siguen, onBack = {}, onMove = { _, _ -> true }, onRemove = {}, onPlay = {}, onClear = {})
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
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), emptyList(), onBack = {}, onMove = { _, _ -> true }, onRemove = {}, onPlay = {}, onClear = {})
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
                QueueScreen(Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), siguen, onBack = {}, onMove = { _, _ -> true }, onRemove = {}, onPlay = { tocada = it }, onClear = {})
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
                    onBack = {}, onMove = { _, _ -> true }, onRemove = {}, onPlay = {}, onClear = { cola = emptyList() },
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

    private var cola by mutableStateOf(larga)
    private var orden: Pair<Int, Int>? = null

    private fun mostrar() = compose.setContent {
        RolaTheme {
            QueueScreen(
                Song(1, "Madrugada", "Ana Vela", null, 0), coloresDePrueba(), cola,
                onBack = {}, onMove = { a, b -> orden = a to b; true }, onRemove = {}, onPlay = {}, onClear = {},
            )
        }
    }

    /** Toma el asa de la fila [titulo], la lleva [dy] dp, la sostiene [sostener] ms y suelta. */
    private fun arrastrar(titulo: String, dy: Float, sostener: Long, alSostener: () -> Unit = {}) {
        val fila = compose.onNodeWithText(titulo).getUnclippedBoundsInRoot()
        val raiz = compose.onRoot()
        // Con el dedo abajo el autodesplazamiento pide cuadros sin parar: el reloj se avanza a mano o la prueba no termina.
        compose.mainClock.autoAdvance = false
        // El asa (48 dp) está pegada al borde derecho, con 8 dp de margen.
        raiz.performTouchInput { down(Offset(width - 32.dp.toPx(), ((fila.top + fila.bottom) / 2).toPx())) }
        // Pasos cortos, un cuadro cada uno, como en el teléfono.
        repeat(40) {
            raiz.performTouchInput { moveBy(Offset(0f, dy.dp.toPx() / 40)) }
            compose.mainClock.advanceTimeByFrame()
        }
        compose.mainClock.advanceTimeBy(sostener)
        alSostener()
        compose.mainClock.advanceTimeByFrame()
        raiz.performTouchInput { up() }
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()
    }

    @Test
    fun arrastradaAlPrimerLugarQuedaEnIndiceCero() {
        // El dedo pasa por «Sonando ahora»: no es destino, la fila queda primera de «A continuación» (FR-059).
        mostrar()
        arrastrar("Canción 3", dy = -900f, sostener = 500)
        assertEquals(3 to 0, orden)
    }

    // shortcut: el desplazamiento automático largo (lista a la mitad hasta arriba o abajo, el video de Brian) se probó en
    // el emulador con la app real; con el reloj manual de Compose la librería avanza ~1 fila/s y una prueba se colgó.
    // Volver a automatizarlo con Maestro si la tarea lo pide.

    @Test
    fun siLaColaCambiaAMitadDelArrastreSeAplicaSobreLaNueva() {
        mostrar()
        // Termina la canción: «Canción 0» pasa a sonar y sale de «A continuación» (FR-058).
        arrastrar("Canción 3", dy = -900f, sostener = 500) { cola = larga.drop(1) }
        assertEquals(2 to 0, orden)
        compose.onNodeWithText("A CONTINUACIÓN · 39").assertIsDisplayed()
    }

    @Test
    fun siLaArrastradaPasaASonarNoSeMueveNada() {
        mostrar()
        arrastrar("Canción 0", dy = 300f, sostener = 500) { cola = larga.drop(1) }
        assertEquals(null, orden)
    }
}
