package com.luis.rola.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlayerSheetTest {

    @get:Rule
    val compose = createComposeRule()

    /** Posición de Reproduciendo 64 ms después de pedir abrir y al terminar. */
    private fun posicionesAlAbrir(reduce: Boolean): Pair<Float, Float> {
        val sheet = PlayerSheetState(abierto = false, colaAbierta = false)
        sheet.cardBounds = Rect(16f, 1500f, 1000f, 1700f)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            RolaTheme {
                CompositionLocalProvider(LocalReduceMotion provides reduce) {
                    PlayerSheet(sheet, Color.Black, card = { Text("Tarjeta") }) {
                        Box(Modifier.fillMaxSize()) { Text("Reproduciendo") }
                    }
                }
            }
        }
        sheet.requestOpen()
        compose.mainClock.advanceTimeBy(64)
        val alInicio = compose.onNodeWithText("Reproduciendo").getUnclippedBoundsInRoot().top.value
        compose.mainClock.advanceTimeBy(2_000)
        val alFinal = compose.onNodeWithText("Reproduciendo").getUnclippedBoundsInRoot().top.value
        return alInicio to alFinal
    }

    @Test
    fun conReducirMovimientoNoSeDesplaza() {
        val (alInicio, alFinal) = posicionesAlAbrir(reduce = true)
        assertEquals(alFinal, alInicio, 0.5f)
    }

    private val abierta = PlayerSheetState(abierto = true, colaAbierta = false).apply {
        cardBounds = Rect(16f, 1500f, 1000f, 1700f)
    }

    private fun montarAbierta() = compose.setContent {
        RolaTheme {
            PlayerSheet(abierta, Color.Black, card = { Text("Tarjeta") }) {
                Box(Modifier.fillMaxSize()) { Text("Reproduciendo") }
            }
        }
    }

    /** [gesto] arrastra Reproduciendo hacia abajo; al terminar todo debe quedar cerrada o abierta. Luego la reabre. */
    private fun assertNuncaAMedias(gesto: TouchInjectionScope.() -> Unit) {
        compose.onRoot().performTouchInput(gesto)
        compose.waitForIdle()
        val p = abierta.expansion.value
        assertTrue("quedó a medias: $p", p == 0f || p == 1f)
        compose.runOnIdle { runBlocking { abierta.expansion.snapTo(1f) } }
        compose.waitForIdle()
    }

    @Test
    fun soltarAMedioCaminoNuncaQuedaAMedias() {
        montarAbierta()
        // Varias distancias y ritmos: cada suelta termina cerrada o abierta (FR-003, SC-012).
        listOf(30f to 50L, 120f to 200L, 400f to 150L, 700f to 600L, 300f to 2_000L).forEach { (px, ms) ->
            assertNuncaAMedias { swipe(center, center + Offset(0f, px), ms) }
        }
    }

    @Test
    fun unGestoCanceladoNoQuedaAMedias() {
        montarAbierta()
        assertNuncaAMedias {
            down(center)
            repeat(10) { moveBy(Offset(0f, 30f)) }
            cancel()
        }
    }

    @Test
    fun sinReducirMovimientoCreceDesdeLaTarjeta() {
        val (alInicio, alFinal) = posicionesAlAbrir(reduce = false)
        assertTrue("debería venir de más abajo: $alInicio vs $alFinal", alInicio > alFinal + 1f)
    }
}
