package com.luis.rola.core.designsystem

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.tween
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionTest {

    @Test
    fun `solo la escala cero pide reducir movimiento`() {
        assertTrue(reduceMotion(0f))
        assertFalse(reduceMotion(1f))
        assertFalse(reduceMotion(0.5f))
    }

    @Test
    fun `con reducir movimiento la animacion salta`() {
        assertTrue(RolaMotion.o(true, tween<Float>(250)) is SnapSpec)
        assertFalse(RolaMotion.o(false, tween<Float>(250)) is SnapSpec)
    }
}
