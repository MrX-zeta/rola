package com.luis.rola.core.data

/** Tema elegido en Ajustes (KV-DATASTORE `theme`). Oscuro por defecto. */
enum class ThemeMode {
    SYSTEM, DARK, LIGHT;

    /** Valor guardado: `system`, `dark` o `light`. */
    fun key(): String = name.lowercase()

    companion object {
        /** Desconocido o nulo = oscuro. */
        fun parse(value: String?): ThemeMode = entries.find { it.key() == value } ?: DARK
    }
}
