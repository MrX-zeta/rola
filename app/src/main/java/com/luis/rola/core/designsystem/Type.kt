package com.luis.rola.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.luis.rola.R

// Fuentes variables empaquetadas en res/font, sin proveedor descargable (INV-01).
@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, peso: FontWeight) =
    Font(res, peso, variationSettings = FontVariation.Settings(FontVariation.weight(peso.weight)))

val SpaceGrotesk = FontFamily(variable(R.font.space_grotesk, FontWeight.Bold))

val Geist = FontFamily(
    variable(R.font.geist, FontWeight.Normal),
    variable(R.font.geist, FontWeight.Medium),
    variable(R.font.geist, FontWeight.SemiBold),
)

val GeistMono = FontFamily(variable(R.font.geist_mono, FontWeight.Medium))

// letterSpacing 0 en todos los estilos, como Figma (DEC-16).
private fun TextStyle.titulo() = copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
private fun TextStyle.interfaz() = copy(fontFamily = Geist, letterSpacing = 0.sp)

private val base = Typography()

val RolaTypography = Typography(
    displayLarge = base.displayLarge.titulo(),
    displayMedium = base.displayMedium.titulo(),
    displaySmall = base.displaySmall.titulo(),
    headlineLarge = base.headlineLarge.titulo(),
    headlineMedium = base.headlineMedium.titulo(),
    headlineSmall = base.headlineSmall.titulo(),
    titleLarge = base.titleLarge.titulo(),
    titleMedium = base.titleMedium.interfaz(),
    titleSmall = base.titleSmall.interfaz(),
    bodyLarge = base.bodyLarge.interfaz(),
    bodyMedium = base.bodyMedium.interfaz(),
    bodySmall = base.bodySmall.interfaz(),
    labelLarge = base.labelLarge.interfaz(),
    labelMedium = base.labelMedium.interfaz(),
    labelSmall = base.labelSmall.interfaz(),
)

private val EstiloTiempo = TextStyle(
    fontFamily = GeistMono,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

/** Para tiempos como «2:47»: Geist Mono Medium, números del mismo ancho. */
val Typography.time: TextStyle get() = EstiloTiempo
