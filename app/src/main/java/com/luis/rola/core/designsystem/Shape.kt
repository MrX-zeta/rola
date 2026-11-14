package com.luis.rola.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val RolaShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Totalmente redonda: botones, chips y la mini tarjeta. */
val Shapes.pill: RoundedCornerShape get() = RoundedCornerShape(percent = 50)
