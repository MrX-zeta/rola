package com.luis.rola.feature.player

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoriteButtonTest {

    @get:Rule
    val compose = createComposeRule()

    private fun montar(reduce: Boolean) = compose.setContent {
        RolaTheme {
            CompositionLocalProvider(LocalReduceMotion provides reduce) {
                var favorita by remember { mutableStateOf(false) }
                FavoriteButton(favorita, { favorita = !favorita })
            }
        }
    }

    private fun estado(s: ToggleableState) = SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, s)

    private fun anchoIcono() = compose.onNodeWithTag("favorita_icono", useUnmergedTree = true).getUnclippedBoundsInRoot().let { (it.right - it.left).value }

    @Test
    fun esUnInterruptorQueCambiaDeEstado() {
        montar(reduce = false)
        val boton = compose.onNodeWithTag("favorita")
        boton.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch)).assert(estado(ToggleableState.Off))
        boton.performClick()
        boton.assert(estado(ToggleableState.On))
        boton.performClick()
        boton.assert(estado(ToggleableState.Off))
    }

    @Test
    fun alMarcarRebotaDesdeMasChico() {
        montar(reduce = false)
        val reposo = anchoIcono()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("favorita").performClick()
        compose.mainClock.advanceTimeBy(50)
        assertTrue(anchoIcono() < reposo)
    }

    @Test
    fun conReducirMovimientoNoEscala() {
        montar(reduce = true)
        val reposo = anchoIcono()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("favorita").performClick()
        compose.mainClock.advanceTimeBy(50)
        assertEquals(reposo, anchoIcono(), 0.01f)
    }
}
