package com.luis.rola.core.data

import java.text.Normalizer

/**
 * Orden por título de DEC-05: acentos fuera, Ñ como letra propia entre N y O,
 * títulos que empiezan con número o símbolo en «#» al final. Sin quitar artículos (PEND-05).
 */
object TitleOrder {

    /** Letra de la sección (A–Z o Ñ) o «#». */
    fun sectionOf(title: String): String {
        val c = title.trimStart().firstOrNull() ?: return "#"
        if (c == 'ñ' || c == 'Ñ') return "Ñ"
        val base = sinAcentos(c.toString()).uppercase()
        return if (base.length == 1 && base[0] in 'A'..'Z') base else "#"
    }

    val comparator: Comparator<String> =
        compareBy<String> { sectionOf(it) == "#" }.thenBy { clave(it) }

    // '{' va justo después de 'z' en ASCII: «ñ» queda tras todas las «n» y antes de «o».
    private fun clave(title: String) = sinAcentos(title.trim().lowercase().replace("ñ", "n{"))

    private val marcas = Regex("\\p{Mn}+")

    private fun sinAcentos(s: String) = Normalizer.normalize(s, Normalizer.Form.NFD).replace(marcas, "")
}
