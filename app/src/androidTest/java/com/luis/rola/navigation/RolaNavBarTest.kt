package com.luis.rola.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.width
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RolaNavBarTest {

    @get:Rule
    val compose = createComposeRule()

    private fun barra(reduce: Boolean = false) = compose.setContent {
        RolaTheme { // RolaTheme fija LocalReduceMotion desde el sistema: se sobrescribe adentro.
            CompositionLocalProvider(LocalReduceMotion provides reduce) {
                RolaNavBar(Destino.Biblioteca.ruta, onSelect = {}, Modifier.testTag("barra"))
            }
        }
    }

    @Test
    fun laSeleccionCambiaEnElPrimerCuadroTrasElToque() {
        barra()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Buscar").performClick() // onSelect no navega: la barra no espera a la ruta
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Buscar").assertIsSelected()
    }

    @Test
    fun conReducirMovimientoQuedaEnSuEstadoFinalDeInmediato() {
        barra(reduce = true)
        compose.mainClock.autoAdvance = false
        val antes = compose.onNodeWithText("Inicio").getUnclippedBoundsInRoot().width
        compose.onNodeWithText("Inicio").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText("Inicio").assertIsSelected()
        // En ese mismo cuadro la pestaña ya tiene su ancho final: no hay cuadros intermedios.
        val ahora = compose.onNodeWithText("Inicio").getUnclippedBoundsInRoot().width
        assertTrue("antes=$antes ahora=$ahora", ahora > antes)
        compose.mainClock.advanceTimeBy(1000)
        assertEquals(ahora, compose.onNodeWithText("Inicio").getUnclippedBoundsInRoot().width)
    }

    @Test
    fun elAnchoDeLaBarraNoCambiaEntrePestanas() {
        barra()
        val ancho = compose.onNodeWithTag("barra").getUnclippedBoundsInRoot().width
        for (t in listOf("Inicio", "Buscar", "Biblioteca")) {
            compose.onNodeWithText(t).performClick()
            compose.waitForIdle()
            compose.onNodeWithTag("barra").assertWidthIsEqualTo(ancho)
        }
    }
}
