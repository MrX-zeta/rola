package com.luis.rola.feature.onboarding

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.luis.rola.core.data.PreferencesRepository
import com.luis.rola.core.data.SongSort
import com.luis.rola.core.data.ThemeMode
import com.luis.rola.core.designsystem.LocalReduceMotion
import com.luis.rola.core.designsystem.RolaMotion
import com.luis.rola.core.designsystem.RolaTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Repositorio falso en memoria: lo que DataStore guardaría. */
private class PreferenciasFalsas(inicial: Boolean) : PreferencesRepository {
    val estado = MutableStateFlow(inicial)
    override val onboardingCompleted = estado
    override suspend fun completeOnboarding() { estado.value = true }
    override val sortOrder = MutableStateFlow(SongSort())
    override suspend fun setSortOrder(sort: SongSort) { sortOrder.value = sort }
    override val theme = MutableStateFlow(ThemeMode.DARK)
    override suspend fun setTheme(mode: ThemeMode) { theme.value = mode }
    override val lastScanAt = MutableStateFlow<Long?>(null)
    override suspend fun setLastScanAt(epochMs: Long) { lastScanAt.value = epochMs }
}

class OnboardingTest {

    @get:Rule
    val compose = createComposeRule()

    /** La puerta como la arma MainActivity, con Permisos detrás (o lo que se pase). */
    private fun puerta(prefs: PreferencesRepository, despues: @Composable () -> Unit = { PermissionScreen {} }) {
        compose.setContent {
            val completado by prefs.onboardingCompleted.collectAsState(null)
            val scope = rememberCoroutineScope()
            RolaTheme { OnboardingGate(completado, { scope.launch { prefs.completeOnboarding() } }, despues) }
        }
    }

    @Test
    fun siguienteHastaLaTerceraMuestraEmpezarYQuitaOmitir() {
        var terminado = false
        compose.setContent { RolaTheme { OnboardingScreen({ terminado = true }) } }
        compose.onNodeWithText("Omitir").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Canta al ritmo.").assertIsDisplayed()
        compose.onNodeWithText("Siguiente").performClick()
        compose.onNodeWithText("Funciona sin internet.").assertIsDisplayed()
        compose.onNodeWithText("Omitir").assertDoesNotExist()
        compose.onNodeWithText("Empezar").performClick()
        assertTrue(terminado)
    }

    @Test
    fun omitirGuardaYLlevaAPermisos() {
        val prefs = PreferenciasFalsas(false)
        puerta(prefs)
        compose.onNodeWithText("Omitir").performClick()
        compose.waitForIdle()
        assertTrue(prefs.estado.value)
        compose.onNodeWithText("Un permiso y listo.").assertIsDisplayed()
        compose.onNodeWithText("Notificaciones").assertDoesNotExist()
        compose.onNodeWithTag("bienvenida").assertDoesNotExist()
    }

    @Test
    fun conReducirMovimientoLaPaginaNuevaSeFundeSinDesplazarse() {
        compose.setContent {
            RolaTheme { CompositionLocalProvider(LocalReduceMotion provides true) { OnboardingScreen({}) } }
        }
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Siguiente").performClick()
        compose.mainClock.advanceTimeBy(RolaMotion.MEDIA / 2L)
        // A mitad del cambio la página 2 ya está en su lugar (solo cambia su opacidad).
        val izquierda = compose.onNodeWithTag("ilustracion-1").getUnclippedBoundsInRoot().left
        assertTrue("se desplazó a $izquierda", izquierda in (-1).dp..1.dp)
        compose.mainClock.autoAdvance = true
        compose.onNodeWithText("Canta al ritmo.").assertIsDisplayed()
    }

    @Test
    fun conBienvenidaCompletadaYPermisoAbreDirecto() {
        val instr = InstrumentationRegistry.getInstrumentation()
        instr.uiAutomation.grantRuntimePermission(instr.targetContext.packageName, musicPermission)
        puerta(PreferenciasFalsas(true)) {
            MusicPermissionGate { Text("Biblioteca", Modifier.testTag("biblioteca")) }
        }
        compose.onNodeWithTag("biblioteca").assertIsDisplayed()
        compose.onNodeWithTag("bienvenida").assertDoesNotExist()
    }

    @Test
    fun mientrasSeLeeNoSeMuestraNada() {
        compose.setContent { RolaTheme { OnboardingGate(null, {}) { Text("Biblioteca", Modifier.testTag("biblioteca")) } } }
        compose.onNodeWithTag("bienvenida").assertDoesNotExist()
        compose.onNodeWithTag("biblioteca").assertDoesNotExist()
    }
}
