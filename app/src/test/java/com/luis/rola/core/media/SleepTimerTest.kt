package com.luis.rola.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepTimerTest {

    private var ahora = 1_000L
    private val timer = SleepTimer { ahora }

    @Test
    fun `cada opcion por tiempo pausa a su hora y vuelve a apagado`() {
        mapOf(
            SleepTimerOption.M15 to 15, SleepTimerOption.M30 to 30, SleepTimerOption.M45 to 45, SleepTimerOption.H1 to 60,
        ).forEach { (opcion, minutos) ->
            ahora = 1_000L
            timer.start(opcion)
            assertEquals(1_000L + minutos * 60_000L, timer.endsAtMs)
            ahora += minutos * 60_000L - 1
            assertFalse(timer.expire())
            assertEquals(opcion, timer.option)
            ahora += 1
            assertTrue(timer.expire())
            assertEquals(SleepTimerOption.OFF, timer.option)
            assertNull(timer.endsAtMs)
        }
    }

    @Test
    fun `al terminar la cancion pausa solo una vez`() {
        timer.start(SleepTimerOption.END_OF_SONG)
        assertNull(timer.endsAtMs)
        ahora += 10 * 60 * 60_000L
        assertFalse(timer.expire()) // no depende del reloj
        assertTrue(timer.songEnded())
        assertEquals(SleepTimerOption.OFF, timer.option)
        assertFalse(timer.songEnded())
    }

    @Test
    fun `apagado cancela y reemplazar reinicia la cuenta`() {
        timer.start(SleepTimerOption.M30)
        ahora += 20 * 60_000L
        timer.start(SleepTimerOption.M15)
        assertEquals(ahora + 15 * 60_000L, timer.endsAtMs)
        timer.start(SleepTimerOption.OFF)
        assertNull(timer.endsAtMs)
        ahora += 60 * 60_000L
        assertFalse(timer.expire())
        assertFalse(timer.songEnded())
    }

    @Test
    fun `una opcion por tiempo no pausa al terminar una cancion`() {
        timer.start(SleepTimerOption.M45)
        assertFalse(timer.songEnded())
        assertEquals(SleepTimerOption.M45, timer.option)
    }
}
