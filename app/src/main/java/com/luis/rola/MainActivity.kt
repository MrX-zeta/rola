package com.luis.rola

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.luis.rola.core.data.ThemeMode
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.feature.onboarding.MusicPermissionGate
import com.luis.rola.feature.onboarding.OnboardingGate
import com.luis.rola.navigation.RolaNavHost
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // null hasta leer DataStore: mientras, sigue el splash del sistema y no parpadea otra pantalla ni otro tema (SC-002).
        // Se lee fuera de Compose: con el splash puesto la vista no dibuja y Compose no avanzaría.
        val completado = mutableStateOf<Boolean?>(null)
        val tema = mutableStateOf<ThemeMode?>(null)
        installSplashScreen().setKeepOnScreenCondition { completado.value == null || tema.value == null }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // de borde a borde desde el primer cuadro; el estilo de las barras lo pone el tema
        val container = (application as RolaApp).container
        lifecycleScope.launch { container.preferences.onboardingCompleted.collect { completado.value = it } }
        lifecycleScope.launch { container.preferences.theme.collect { tema.value = it } }
        setContent {
            val oscuro = when (tema.value ?: return@setContent) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }
            // Íconos de las barras del sistema según el tema elegido, no el del teléfono (FR-012).
            LaunchedEffect(oscuro) {
                val barras = if (oscuro) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = barras, navigationBarStyle = barras)
            }
            RolaTheme(oscuro) {
                OnboardingGate(completado.value, onComplete = { lifecycleScope.launch { container.preferences.completeOnboarding() } }) {
                    MusicPermissionGate { RolaNavHost(container) }
                }
            }
        }
    }
}
