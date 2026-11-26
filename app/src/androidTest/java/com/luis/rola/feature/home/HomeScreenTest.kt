package com.luis.rola.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.luis.rola.core.data.MostPlayed
import com.luis.rola.core.data.Song
import com.luis.rola.core.designsystem.RolaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

class HomeScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val luz = Song(1, "Luz de neón", "Las Nubes", null, 0)
    private val camino = Song(2, "Camino lento", "Marea Alta", null, 0)
    private val toques = mutableListOf<String>()

    private fun montar(state: HomeState, scanning: Boolean = false) = compose.setContent { Inicio(state, scanning) }

    @Composable
    private fun Inicio(state: HomeState, scanning: Boolean) = RolaTheme {
        HomeScreen(
            state,
            onPlay = { toques += it.title },
            onShuffle = { toques += "aleatorio" },
            onFavorites = { toques += "favoritas" },
            onLibrary = { toques += "biblioteca" },
            onScan = { toques += "escanear" },
            onSettings = { toques += "ajustes" },
            scanning = scanning,
            now = LocalDateTime.of(2026, 11, 20, 21, 30),
        )
    }

    private fun noEstaLoQueAunNoExiste() =
        listOf("Ver todo", "Letras").forEach { compose.onNodeWithText(it).assertDoesNotExist() }

    @Test
    fun conHistorial() {
        montar(HomeState.Content(listOf(luz, camino), MostPlayed(camino, 48), songs = 312, favorites = 1))
        compose.onNodeWithText("Viernes, 20 de noviembre").assertIsDisplayed()
        compose.onNodeWithText("Buenas noches").assertIsDisplayed()
        compose.onAllNodesWithTag("reciente").assertCountEquals(2)
        val izquierda = { t: String ->
            compose.onNode(hasText(t) and hasAnyAncestor(hasTestTag("recientes")), useUnmergedTree = true).getUnclippedBoundsInRoot().left
        }
        assertTrue(izquierda("Luz de neón") < izquierda("Camino lento"))
        compose.onNodeWithText("Marea Alta · 48 veces").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("312 canciones").performScrollTo()
        compose.onNodeWithText("1 canción").assertExists()
        compose.onNodeWithTag("primera_vez").assertDoesNotExist()
        noEstaLoQueAunNoExiste()

        compose.onNodeWithText("Luz de neón").performScrollTo().performClick()
        compose.onNodeWithTag("mas_escuchada").performScrollTo().performClick()
        compose.onNodeWithText("Aleatorio").performScrollTo().performClick()
        compose.onNodeWithText("Favoritas").performClick()
        assertEquals(listOf("Luz de neón", "Camino lento", "aleatorio", "favoritas"), toques)
    }

    @Test
    fun laPrimeraVezInvitaSinCarruselNiDestacado() {
        montar(HomeState.Content(emptyList(), null, songs = 312, favorites = 0))
        compose.onNodeWithText("Aún no escuchas nada").assertIsDisplayed()
        compose.onNodeWithText("Escuchado hace poco").assertDoesNotExist()
        compose.onNodeWithTag("mas_escuchada").assertDoesNotExist()
        compose.onNodeWithText("312 canciones").assertExists()
        compose.onNodeWithText("Favoritas").assertExists()
        noEstaLoQueAunNoExiste()
        compose.onNode(hasText("Aleatorio") and hasAnyAncestor(hasTestTag("primera_vez"))).performClick()
        compose.onNodeWithText("Ir a Biblioteca").performClick()
        assertEquals(listOf("aleatorio", "biblioteca"), toques)
    }

    @Test
    fun sinCancionesOfreceEscanear() {
        montar(HomeState.NoSongs)
        compose.onNodeWithText("No encontramos música").assertIsDisplayed()
        compose.onNodeWithText("Buenas noches").assertDoesNotExist()
        noEstaLoQueAunNoExiste()
        compose.onNodeWithText("Escanear de nuevo").performClick()
        assertEquals(listOf("escanear"), toques)
    }

    @Test
    fun escaneandoMuestraProgresoYNoAceptaOtroToque() {
        montar(HomeState.NoSongs, scanning = true)
        compose.onNodeWithText("Escaneando…").assertIsNotEnabled()
        compose.onNodeWithText("Escanear de nuevo").assertDoesNotExist()
    }

    @Test
    fun elEngraneYAbrirAjustesLlevanAAjustes() {
        montar(HomeState.NoSongs)
        compose.onNodeWithTag("ajustes_engrane").performClick()
        compose.onNodeWithTag("abrir_ajustes").performScrollTo().performClick()
        assertEquals(listOf("ajustes", "ajustes"), toques)
    }
}
