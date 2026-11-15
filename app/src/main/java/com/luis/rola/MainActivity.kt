package com.luis.rola

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.feature.onboarding.MusicPermissionGate
import com.luis.rola.feature.onboarding.OnboardingGate
import com.luis.rola.navigation.RolaNavHost
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // null hasta leer DataStore: mientras, sigue el splash del sistema y no parpadea otra pantalla (SC-002).
        // Se lee fuera de Compose: con el splash puesto la vista no dibuja y Compose no avanzaría.
        val completado = mutableStateOf<Boolean?>(null)
        installSplashScreen().setKeepOnScreenCondition { completado.value == null }
        super.onCreate(savedInstanceState)
        // Tema oscuro fijo por ahora (RolaTheme): barras con íconos claros aunque el sistema esté en claro.
        val barras = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barras, navigationBarStyle = barras)
        val container = (application as RolaApp).container
        lifecycleScope.launch { container.preferences.onboardingCompleted.collect { completado.value = it } }
        setContent {
            RolaTheme {
                OnboardingGate(completado.value, onComplete = { lifecycleScope.launch { container.preferences.completeOnboarding() } }) {
                    MusicPermissionGate { RolaNavHost(container) }
                }
            }
        }
    }
}
