package com.luis.rola.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RolaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), // portadas pequeñas de lista (DEC-16)
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Totalmente redonda: botones, chips y la mini tarjeta. */
val Shapes.pill: RoundedCornerShape get() = RoundedCornerShape(percent = 50)

/** 16 dp: portadas de lista de 56 dp y el menú de opciones (Figma 32:1165, 35:1626). */
val Shapes.tile: RoundedCornerShape get() = RoundedCornerShape(16.dp)
