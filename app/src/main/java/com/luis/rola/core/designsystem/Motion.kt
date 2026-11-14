package com.luis.rola.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.runtime.staticCompositionLocalOf

/** Movimiento de DEC-28: ≤ 300 ms, salidas más cortas que entradas, resortes interrumpibles. */
object RolaMotion {
    /** Entradas y salidas. */
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    /** Salidas y cambios de ícono. */
    const val RAPIDA = 100

    /** Fundidos de entrada y cambios de color. */
    const val MEDIA = 150

    /** Entradas con desplazamiento. */
    const val LENTA = 250

    /** ≈ 0.3 s con rebote sutil; si se interrumpe, sigue desde donde va. */
    fun <T> resorte(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 400f)

    /** Con «reducir movimiento», salto instantáneo; si no, [spec]. */
    fun <T> o(reduce: Boolean, spec: FiniteAnimationSpec<T>): FiniteAnimationSpec<T> = if (reduce) snap() else spec
}

/** «Quitar animaciones» de Android pone la escala del animador en 0 (INV-06). */
fun reduceMotion(animatorScale: Float) = animatorScale == 0f

/** `true` si el sistema pide reducir movimiento: sin desplazamiento ni escala, solo instantáneo o fundido corto. */
val LocalReduceMotion = staticCompositionLocalOf { false }
