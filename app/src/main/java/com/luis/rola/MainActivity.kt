package com.luis.rola

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.luis.rola.core.designsystem.RolaTheme
import com.luis.rola.feature.onboarding.MusicPermissionGate
import com.luis.rola.navigation.RolaNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Tema oscuro fijo por ahora (RolaTheme): barras con íconos claros aunque el sistema esté en claro.
        val barras = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barras, navigationBarStyle = barras)
        val container = (application as RolaApp).container
        setContent {
            RolaTheme {
                MusicPermissionGate { RolaNavHost(container) }
            }
        }
    }
}
