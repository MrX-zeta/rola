package com.luis.rola.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
    surfaceTint = Color(0xFFC7FF2E),
    inverseSurface = Color(0xFFFAF8F2),
    inverseOnSurface = Color(0xFF10100E),
    inversePrimary = Color(0xFF3D5200),
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
    surfaceTint = Color(0xFF3D5200),
    inverseSurface = Color(0xFF10100E),
    inverseOnSurface = Color(0xFFFAF8F2),
    inversePrimary = Color(0xFFC7FF2E),
)
