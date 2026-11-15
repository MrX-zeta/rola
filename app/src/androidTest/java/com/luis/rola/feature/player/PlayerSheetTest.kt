package com.luis.rola.feature.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaTheme
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

    @Test
    fun sinReducirMovimientoCreceDesdeLaTarjeta() {
        val (alInicio, alFinal) = posicionesAlAbrir(reduce = false)
        assertTrue("debería venir de más abajo: $alInicio vs $alFinal", alInicio > alFinal + 1f)
    }
}
