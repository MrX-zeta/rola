package com.luis.rola.feature.player

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

/** Fracción del recorrido que confirma un gesto (DEC-04, revisión del Redmi 2026-11-14). */
const val SWIPE_FRACCION = 0.25f

/** Tope de esa distancia, en dp: en una tarjeta ancha basta un deslizamiento corto, empiece donde empiece. */
const val SWIPE_DISTANCIA_DP = 96f

/** Velocidad que confirma un gesto aunque sea corto, en dp/s. */
const val SWIPE_VELOCIDAD_DP = 400f

/**
 * ¿El gesto se confirma al soltar? Sí si el dedo recorrió, desde donde empezó, el menor entre [SWIPE_FRACCION] de
 * [travel] y [SWIPE_DISTANCIA_DP], o se lanzó a [SWIPE_VELOCIDAD_DP] en su dirección; un lanzamiento en contra lo
 * cancela aunque haya pasado la distancia. [offset] y [velocity] en px y px/s, con el mismo signo si van al mismo lado.
 */
fun swipeCommits(offset: Float, velocity: Float, travel: Float, density: Float): Boolean {
    if (offset == 0f) return false
    val aFavor = velocity * sign(offset)
    val minima = SWIPE_VELOCIDAD_DP * density
    val distancia = min(SWIPE_FRACCION * travel, SWIPE_DISTANCIA_DP * density)
    return aFavor > -minima && (abs(offset) >= distancia || aFavor >= minima)
}
