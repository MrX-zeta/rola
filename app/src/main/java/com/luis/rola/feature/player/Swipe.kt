package com.luis.rola.feature.player

import kotlin.math.abs
import kotlin.math.sign

/** Fracción del recorrido que confirma un gesto (DEC-04). Calibrar en el Redmi. */
const val SWIPE_FRACCION = 0.35f

/** Velocidad que confirma un gesto aunque sea corto, en dp/s. */
const val SWIPE_VELOCIDAD_DP = 700f

/**
 * ¿El gesto se confirma al soltar? Sí si avanzó [SWIPE_FRACCION] de [travel] o se lanzó a [SWIPE_VELOCIDAD_DP]
 * en su dirección; un lanzamiento en contra lo cancela aunque haya pasado la distancia.
 * [offset] y [velocity] en px y px/s, con el mismo signo cuando van hacia el mismo lado.
 */
fun swipeCommits(offset: Float, velocity: Float, travel: Float, density: Float): Boolean {
    if (offset == 0f) return false
    val aFavor = velocity * sign(offset)
    val minima = SWIPE_VELOCIDAD_DP * density
    return aFavor > -minima && (abs(offset) >= SWIPE_FRACCION * travel || aFavor >= minima)
}
