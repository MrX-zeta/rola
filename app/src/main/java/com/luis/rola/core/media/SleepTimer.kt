package com.luis.rola.core.media

/** Opciones de SCR-53. Los minutos solo cuentan para las opciones por tiempo. */
enum class SleepTimerOption(val minutes: Int) { OFF(0), M15(15), M30(30), M45(45), H1(60), END_OF_SONG(0) }

/** Temporizador de apagado (OP-15). Puro: el reloj lo pone [PlaybackService] (`elapsedRealtime`). */
class SleepTimer(private val now: () -> Long) {

    var option = SleepTimerOption.OFF
        private set

    /** Hora de pausar, en el reloj de [now]; `null` si no es una opción por tiempo. */
    var endsAtMs: Long? = null
        private set

    /** Inicia, reemplaza o cancela ([SleepTimerOption.OFF]). */
    fun start(option: SleepTimerOption) {
        this.option = option
        endsAtMs = if (option.minutes > 0) now() + option.minutes * 60_000L else null
    }

    /** Se cumplió la hora: `true` si toca pausar; vuelve a apagado. */
    fun expire(): Boolean = (endsAtMs?.let { now() >= it } == true).also { if (it) start(SleepTimerOption.OFF) }

    /** Terminó una canción: `true` si la opción era «al terminar la canción»; vuelve a apagado. */
    fun songEnded(): Boolean = (option == SleepTimerOption.END_OF_SONG).also { if (it) start(SleepTimerOption.OFF) }
}
