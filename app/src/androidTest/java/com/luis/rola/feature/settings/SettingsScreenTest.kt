package com.luis.rola.feature.settings

import android.os.SystemClock
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.luis.rola.BuildConfig
import com.luis.rola.core.data.ThemeMode
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.core.media.SleepTimerOption
import com.luis.rola.feature.library.PreferenciasEnMemoria
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val prefs = PreferenciasEnMemoria()
    private val toques = mutableListOf<String>()

    private fun montar(
        lastScanAt: Long? = null,
        scanning: Boolean = false,
        timer: SleepTimerOption = SleepTimerOption.OFF,
        endsAt: Long? = null,
    ) = compose.setContent {
        val tema by prefs.theme.collectAsState()
        RolaTheme {
            SettingsScreen(
                theme = tema,
                lastScanAt = lastScanAt,
                scanning = scanning,
                timer = timer,
                timerEndsAtMs = endsAt,
                onBack = { toques += "atras" },
                onTheme = { runBlocking { prefs.setTheme(it) } },
                onScan = { toques += "escanear" },
                onTimer = { toques += "temporizador $it" },
                onOpenDoc = { toques += it.name },
            )
        }
    }

    @Test
    fun soloLasFilasQueYaExisten() {
        montar()
        listOf("Temporizador de apagado", "Escanear medios", "Tema", "Términos y condiciones", "Aviso de privacidad", "Licencias", "Versión")
            .forEach { compose.onNodeWithText(it).assertExists() }
        listOf("Otros avisos", "Carpeta de letras", "Buscar letras en internet", "Letras descargadas", "Agregar widget", "LETRAS")
            .forEach { compose.onNodeWithText(it).assertDoesNotExist() }
        compose.onNodeWithText("Rola ${BuildConfig.VERSION_NAME}").assertExists()
    }

    @Test
    fun elTemaEmpiezaOscuroYElegirClaroSeGuarda() {
        montar()
        compose.onNodeWithText("Oscuro").assertExists()
        compose.onNodeWithText("Tema").performScrollTo().performClick()
        compose.onNodeWithText("Claro").performClick()
        compose.waitForIdle()
        assertEquals(ThemeMode.LIGHT, prefs.theme.value)
        compose.onNodeWithText("Claro").assertExists() // la fila ya dice el valor nuevo
        compose.onNodeWithText("Oscuro").assertDoesNotExist()
    }

    @Test
    fun sinEscaneo() {
        montar(lastScanAt = null)
        compose.onNodeWithText("Aún no has escaneado").assertExists()
    }

    @Test
    fun ultimoEscaneoHaceDosDias() {
        montar(lastScanAt = System.currentTimeMillis() - 2 * 24 * 3_600_000L - 60_000)
        compose.onNodeWithText("Último escaneo: hace 2 días").assertExists()
    }

    @Test
    fun escaneandoNoAceptaOtroToque() {
        montar(scanning = true)
        compose.onNodeWithText("Escanear medios").performScrollTo().performClick()
        assertTrue(toques.isEmpty())
    }

    @Test
    fun escanearLlamaUnaVez() {
        montar()
        compose.onNodeWithText("Escanear medios").performScrollTo().performClick()
        assertEquals(listOf("escanear"), toques)
    }

    @Test
    fun temporizadorApagado() {
        montar(timer = SleepTimerOption.OFF)
        compose.onNodeWithText("Apagado").assertExists()
    }

    @Test
    fun temporizadorAlTerminarLaCancion() {
        montar(timer = SleepTimerOption.END_OF_SONG)
        compose.onNodeWithText("Al terminar la canción").assertExists()
        compose.onNodeWithText("Apagado").assertDoesNotExist()
    }

    @Test
    fun temporizadorConTiempoYAbreLaHoja() {
        montar(timer = SleepTimerOption.M15, endsAt = SystemClock.elapsedRealtime() + 15 * 60_000L)
        compose.onNodeWithText("Apagado").assertDoesNotExist()
        compose.onNodeWithText("Temporizador de apagado").performClick()
        compose.onNodeWithText("Iniciar temporizador").assertIsDisplayed()
        compose.onNodeWithText("Iniciar temporizador").performClick()
        assertEquals(listOf("temporizador M15"), toques)
    }

    @Test
    fun losDocumentosSeAbren() {
        montar()
        compose.onNodeWithText("Términos y condiciones").performScrollTo().performClick()
        compose.onNodeWithText("Aviso de privacidad").performScrollTo().performClick()
        compose.onNodeWithText("Licencias").performScrollTo().performClick()
        assertEquals(listOf("TERMS", "PRIVACY", "LICENSES"), toques)
    }
}
