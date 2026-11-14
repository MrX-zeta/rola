package com.luis.rola.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Oscuro es la base (DEC-15). El ajuste Sistema/Oscuro/Claro llega con Ajustes.
 * Sin color dinámico del fondo de pantalla.
 */
@Composable
fun RolaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) RolaDarkColors else RolaLightColors,
        typography = RolaTypography,
        shapes = RolaShapes,
        content = content,
    )
}
