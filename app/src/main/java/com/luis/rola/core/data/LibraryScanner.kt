package com.luis.rola.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

/** «Escanear medios» (OP-16) compartido por Inicio y Ajustes: uno a la vez y guarda cuándo terminó (FR-042). */
class LibraryScanner(
    private val songs: SongRepository,
    private val preferences: PreferencesRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val ocupado = Mutex()
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    /** Canciones tras escanear; `null` si ya había uno en curso (no se repite ni se avisa dos veces). */
    suspend fun scan(): Int? {
        if (!ocupado.tryLock()) return null
        _scanning.value = true
        try {
            return songs.scan().also { preferences.setLastScanAt(now()) }
        } finally {
            _scanning.value = false
            ocupado.unlock()
        }
    }
}
