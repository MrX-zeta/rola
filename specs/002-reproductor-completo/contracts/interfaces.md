# Contratos internos: Reproductor completo

Rola no expone API a otros sistemas. Contratos entre capas; firmas orientativas.

## PlayerConnection (`core/media`, ampliado)

```kotlin
class PlayerConnection {
    val state: StateFlow<PlayerState>          // ver data-model.md
    val queue: StateFlow<QueueState>           // nuevo
    val errors: SharedFlow<Unit>               // existente
    fun play(queue: List<Song>, startIndex: Int)   // existente
    fun shuffle(queue: List<Song>)                 // existente: «Aleatorio» de Biblioteca (no cambia el modo)
    fun togglePlayPause()                          // existente
    fun next()                                     // existente
    fun previous()                                 // seekToPrevious (FR-012)
    fun seekTo(positionMs: Long)
    fun toggleShuffle()
    fun cycleRepeat()                              // OFF → ALL → ONE → OFF
    fun moveUpNext(from: Int, to: Int)             // posiciones en upNext
    fun removeUpNext(position: Int): Removed?      // para Deshacer
    fun undoRemove(removed: Removed)
    fun setSleepTimer(option: SleepTimerOption)
}
```

- Ninguna llamada detiene la música; solo `togglePlayPause` y el temporizador pausan (INV-11).
- Con el controlador sin conectar, las órdenes se ignoran (atajo ya documentado en la tarea 02).

## PlaybackService (`core/media`, ampliado)

Comandos propios (`SessionCommand`), disponibles solo para controladores con `packageName` de Rola:

| Acción | Argumentos (`Bundle`) | Efecto | Validación |
|---|---|---|---|
| `rola.ORDEN_ALEATORIO` | `orden: IntArray` | `setShuffleOrder(DefaultShuffleOrder(orden, semilla))` | Permutación exacta de `0 until mediaItemCount`; si no, `RESULT_ERROR_BAD_VALUE`. |
| `rola.TEMPORIZADOR` | `opcion: String` (nombre de `SleepTimerOption`) | Inicia, cambia o cancela el temporizador | Nombre válido; si no, `RESULT_ERROR_BAD_VALUE`. |

Extras de sesión publicados: `temporizador` (nombre de la opción) y `terminaEn` (`Long`, `elapsedRealtime`, ausente si no aplica).

Reglas del servicio:
- Al encender el aleatorio o cargar una lista nueva con él encendido: orden aleatorio que empieza en la canción actual.
- Al cumplirse el temporizador: `pause()`, nunca `stop()`.
- `onDestroy`: cancela el temporizador.
- Sin permisos nuevos en el manifiesto.

## Funciones puras (JUnit)

| Función | Paquete | Contrato |
|---|---|---|
| `QueueOrder.upNext(current, next: (Int) -> Int)` | `core/media` | Índices desde el siguiente de `current` hasta `C.INDEX_UNSET`. |
| `QueueOrder.move(order, currentPos, from, to)` | `core/media` | Mueve dentro de «A continuación» (posiciones relativas a la actual); el resto no cambia. |
| `QueueOrder.shuffledStartingWith(current, size, random)` | `core/media` | Permutación que empieza en `current` y contiene cada índice una vez. |
| `QueueOrder.isPermutation(order, size)` | `core/media` | `true` solo si cada índice de `0 until size` aparece una vez. |
| `SleepTimer.start(option, now)` / `endsAt` / `isDue(now)` / `onSongEnded()` | `core/media` | Ver data-model.md. |
| `swipeDecision(offset, velocity, travel, density)` | `feature/player` | `COMMIT` si la distancia ≥ 35 % del recorrido o la velocidad ≥ 700 dp/s en la dirección del gesto; `CANCEL` con velocidad en contra o por debajo de ambos. |
| `formatDuration(ms)` | `core/media` | Existente: «m:ss» / «h:mm:ss». |

## Interfaz (Compose)

| Composable | Parámetros clave | Prueba |
|---|---|---|
| `NowPlayingScreen` | `state`, `colors`, `progress`, callbacks (`onClose`, `onToggle`, `onPrevious`, `onNext`, `onSeek`, `onShuffle`, `onRepeat`, `onOpenQueue`, `onOpenTimer`) | Muestra título, tiempos, estados de aleatorio y repetir (descripciones). |
| `PlayerSheet` | `state`, `expansion`, contenido | Con `LocalReduceMotion = true`, el contenido no cambia de posición durante la transición. |
| `QueueScreen` | `current`, `upNext`, callbacks (`onMove`, `onRemove`, `onBack`) | Muestra la canción actual en «Sonando ahora». |
| `SleepTimerSheet` | `timer`, `onStart(option)`, `onDismiss` | (manual) |

Etiquetas de prueba (`testTag`): `reproduciendo`, `cola`, `mini`, `barraAvance`.
