package com.luis.rola.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Neutros cálidos de DEC-16 (docs/marca/README.md). Único lugar con hex (INV-04).
// Los roles de M3 que la marca no define se derivan de estos mismos tokens.
// Sin color dinámico: Material You apagado (DEC-15).

val RolaDarkColors: ColorScheme = darkColorScheme(
    background = Color(0xFF10100E),
    onBackground = Color(0xFFFAF8F2),
    surface = Color(0xFF1B1A17),
    onSurface = Color(0xFFFAF8F2),
    surfaceVariant = Color(0xFF262521),
    onSurfaceVariant = Color(0xFFA9A69C),
    surfaceDim = Color(0xFF10100E),
    surfaceBright = Color(0xFF262521),
    surfaceContainerLowest = Color(0xFF10100E),
    surfaceContainerLow = Color(0xFF1B1A17),
    surfaceContainer = Color(0xFF262521),
    surfaceContainerHigh = Color(0xFF262521),
    surfaceContainerHighest = Color(0xFF262521),
    outline = Color(0xFF34322D),
    outlineVariant = Color(0xFF34322D),
    primary = Color(0xFFC7FF2E),
    onPrimary = Color(0xFF10100E),
    primaryContainer = Color(0xFF27310C),
    onPrimaryContainer = Color(0xFFC7FF2E),
    secondary = Color(0xFFA9A69C),
    onSecondary = Color(0xFF10100E),
    secondaryContainer = Color(0xFF262521),
    onSecondaryContainer = Color(0xFFFAF8F2),
    tertiary = Color(0xFFC7FF2E),
    onTertiary = Color(0xFF10100E),
    tertiaryContainer = Color(0xFF27310C),
    onTertiaryContainer = Color(0xFFC7FF2E),
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFFFAF8F2),
    inverseOnSurface = Color(0xFF10100E),
    inversePrimary = Color(0xFF3D5200),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

val RolaLightColors: ColorScheme = lightColorScheme(
    background = Color(0xFFF6F6F4),
    onBackground = Color(0xFF10100E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF10100E),
    surfaceVariant = Color(0xFFECEBE6),
    onSurfaceVariant = Color(0xFF5C5A53),
    surfaceDim = Color(0xFFECEBE6),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF6F6F4),
    surfaceContainer = Color(0xFFECEBE6),
    surfaceContainerHigh = Color(0xFFECEBE6),
    surfaceContainerHighest = Color(0xFFECEBE6),
    outline = Color(0xFFD9D7D0),
    outlineVariant = Color(0xFFD9D7D0),
    primary = Color(0xFF3D5200),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC7FF2E),
    onPrimaryContainer = Color(0xFF10100E),
    secondary = Color(0xFF5C5A53),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECEBE6),
    onSecondaryContainer = Color(0xFF10100E),
    tertiary = Color(0xFF3D5200),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC7FF2E),
    onTertiaryContainer = Color(0xFF10100E),
    surfaceTint = Color.Transparent,
    inverseSurface = Color(0xFF10100E),
    inverseOnSurface = Color(0xFFFAF8F2),
    inversePrimary = Color(0xFFC7FF2E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

/** Colores de marca que M3 no tiene: relleno de acciones principales (DEC-16). Iguales en claro y oscuro. */
@Immutable
data class RolaColors(
    val accent: Color = Color(0xFFC7FF2E),
    val onAccent: Color = Color(0xFF10100E),
    /** Círculo translúcido de los botones sobre la portada (corazón de DEC-29, Figma 78:3335). */
    val overCover: Color = Color(0xB3111111),
    val onOverCover: Color = Color(0xFFFAF8F2),
)

val LocalRolaColors = staticCompositionLocalOf { RolaColors() }

/** Portadas de ejemplo de las ilustraciones del onboarding (componente «Portada» 5:44 de Figma). Iguales en claro y oscuro. */
object RolaIlustracion {
    val azul = Color(0xFF2F4BB8)
    val azulClaro = Color(0xFF9FB4FF)
    val naranja = Color(0xFFE4572E)
    val naranjaClaro = Color(0xFFF3E9D2)
    val verde = Color(0xFF1F7A5C)
    val verdeClaro = Color(0xFFD9F2B4)
}
