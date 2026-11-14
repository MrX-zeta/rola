# Contratos internos: Reproductor base

Rola no expone API a otros sistemas. Estos son los contratos entre capas que usan los ViewModel y las pruebas (falsos). Firmas orientativas; el detalle va en la implementación.

## SongRepository (`core/data`)

```kotlin
interface SongRepository {
    /** Canciones con IS_MUSIC != 0, ordenadas por título (DEC-05). Emite de nuevo cuando cambia el MediaStore (DEC-26). */
    fun songs(): Flow<List<Song>>
}
```

- Solo se llama con el permiso de música concedido.
- No lanza por falta de permiso: emite lista vacía y deja que la interfaz decida.
- Falso de pruebas: `MutableStateFlow<List<Song>>`.

## PlayerConnection (`core/media`)

```kotlin
interface PlayerConnection {
    val state: StateFlow<PlayerState>      // ver data-model.md
    val errors: Flow<Unit>                 // una emisión por canción que no se pudo reproducir (FR-018)
    fun play(queue: List<Song>, startIndex: Int)
    fun shuffle(queue: List<Song>)         // cola = lista barajada, empieza en la primera
    fun togglePlayPause()
    fun next()
}
```

- Implementación real: `MediaController` conectado a `PlaybackService`; se conecta al iniciar la app y se libera con el proceso.
- Ninguna llamada detiene la música salvo `togglePlayPause` (INV-11).

## PlaybackService (`core/media`)

- Declarado en el manifiesto con `android:foregroundServiceType="mediaPlayback"`, `exported="true"` y el filtro `androidx.media3.session.MediaSessionService`.
- Permisos que agrega esta tarea: `READ_MEDIA_AUDIO`, `READ_EXTERNAL_STORAGE` (`maxSdkVersion="32"`), `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `WAKE_LOCK`. **REQUIERE REVISIÓN HUMANA** (permisos).
- Sin `INTERNET` (INV-01).

## Funciones puras (probadas con JUnit)

| Función | Paquete | Contrato |
|---|---|---|
| `TitleOrder.comparator` / `sectionOf(title)` | `core/data` | DEC-05: acentos fuera, Ñ entre N y O, números y símbolos en «#» al final. |
| `songFrom(...)` | `core/data` | Reglas de [data-model.md](../data-model.md) para título y artista. |
| `formatDuration(ms)` | `core/media` | `0` → «0:00»; `65_000` → «1:05»; ≥ 1 h → «1:01:05». Se usa en la descripción accesible del progreso de la mini tarjeta. |
| `ensureContrast(fg, bg, min = 4.5)` | `core/designsystem` | Devuelve `fg` si ya cumple; si no, el color más cercano en luminosidad que cumple. |

## Interfaz (Compose)

| Composable | Entrada | Pantalla |
|---|---|---|
| `PermissionScreen(onAllow)` | — | SCR-05 |
| `PermissionDeniedScreen(onOpenSettings, onRetry)` | — | SCR-06 |
| `LibraryScreen(songs, onSongClick, onShuffle)` | lista o `null` mientras carga | SCR-20 |
| `MiniPlayer(state, onToggle, onNext)` | `PlayerState`; no dibuja nada si `current == null` | SCR-50 |
| `ComingSoonScreen()` | — | Inicio y Buscar |
