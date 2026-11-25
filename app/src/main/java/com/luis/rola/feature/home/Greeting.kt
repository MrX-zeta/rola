package com.luis.rola.feature.home

import com.luis.rola.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Saludo según la hora (DEC-30): 5–11 días, 12–18 tardes, 19–4 noches. */
fun greeting(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.buenos_dias
    in 12..18 -> R.string.buenas_tardes
    else -> R.string.buenas_noches
}

private val formato = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es"))

/** «Viernes, 20 de noviembre». */
fun dateText(now: LocalDateTime): String = formato.format(now).replaceFirstChar { it.uppercase() }
