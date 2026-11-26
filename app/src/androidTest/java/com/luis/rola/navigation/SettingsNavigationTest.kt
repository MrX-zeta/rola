package com.luis.rola.navigation

import android.Manifest
import android.os.Build
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.luis.rola.MainActivity
import com.luis.rola.RolaApp
import com.luis.rola.core.data.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Engrane → Ajustes → atrás con la app real, y el tema elegido se guarda (FR-001, FR-013). */
class SettingsNavigationTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instr = InstrumentationRegistry.getInstrumentation()
    private val app = instr.targetContext.applicationContext as RolaApp
    private val titulo = hasText("Ajustes") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading)

    @Before
    fun preparar() = runBlocking {
        val permiso = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
        instr.uiAutomation.grantRuntimePermission(app.packageName, permiso)
        app.container.preferences.completeOnboarding()
        app.container.preferences.setTheme(ThemeMode.DARK)
    }

    @After
    fun dejarOscuro() = runBlocking { app.container.preferences.setTheme(ThemeMode.DARK) }

    @Test
    fun elEngraneAbreAjustesComoHijaDeInicioYAtrasRegresa() {
        ActivityScenario.launch(MainActivity::class.java).use {
            compose.onNodeWithTag("ajustes_engrane").performClick()
            compose.onNode(titulo).assertIsDisplayed()
            compose.onNodeWithText("Inicio").assertIsSelected() // la píldora no salta
            Espresso.pressBack()
            compose.onNode(titulo).assertDoesNotExist()
            compose.onNodeWithTag("ajustes_engrane").assertIsDisplayed()
        }
    }

    @Test
    fun elTemaElegidoSeGuardaYSigueAlRecrear() {
        ActivityScenario.launch(MainActivity::class.java).use { actividad ->
            compose.onNodeWithTag("ajustes_engrane").performClick()
            compose.onNodeWithText("Tema").performClick()
            compose.onNodeWithText("Claro").performClick()
            compose.waitUntil(5_000) { runBlocking { app.container.preferences.theme.first() } == ThemeMode.LIGHT }
            actividad.recreate()
            compose.onNode(titulo).assertIsDisplayed()
            compose.onNodeWithText("Claro").assertIsDisplayed() // Ajustes vuelve con el valor guardado
        }
    }
}
