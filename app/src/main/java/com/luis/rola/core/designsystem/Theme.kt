package com.luis.rola.core.designsystem

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext

/**
 * Oscuro es la base (DEC-15). El ajuste Sistema/Oscuro/Claro llega con Ajustes.
 * Sin color dinámico del fondo de pantalla.
 */
@Composable
fun RolaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val resolver = LocalContext.current.contentResolver
    val escala = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    CompositionLocalProvider(
        LocalRolaColors provides RolaColors(),
        LocalReduceMotion provides reduceMotion(escala),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) RolaDarkColors else RolaLightColors,
            typography = RolaTypography,
            shapes = RolaShapes,
            content = content,
        )
    }
}

object RolaTheme {
    val colors: RolaColors
        @Composable @ReadOnlyComposable get() = LocalRolaColors.current
}
