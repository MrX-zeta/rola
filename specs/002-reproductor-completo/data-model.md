# Modelo de datos: Reproductor completo

Nada se guarda en disco. Todo vive en memoria: en `PlaybackService` (fuente de verdad) y, como reflejo, en `PlayerConnection`.

## PlayerState (`core/media`, ampliado)

| Campo | Tipo | Regla |
|---|---|---|
| `current` | `Song?` | `null` → no hay nada cargado; sin tarjeta ni Reproduciendo. |
| `isPlaying` | `Boolean` | |
| `positionMs` | `Long` | Posición leída en el último evento. |
| `positionAtMs` | `Long` | **Nuevo.** `elapsedRealtime` en que se leyó `positionMs`; la interfaz extrapola mientras suena. |
| `durationMs` | `Long` | `0` si se desconoce (barra deshabilitada). |
| `hasNext` | `Boolean` | Respeta repetir «toda la cola». |
| `shuffle` | `Boolean` | **Nuevo.** Modo aleatorio. |
| `repeat` | `Repeat` | **Nuevo.** `OFF`, `ALL`, `ONE`. |
| `timer` | `SleepTimerState` | **Nuevo.** Ver abajo. |

Se publica solo en eventos del reproductor y de la sesión (sin ticker).

## QueueState (`core/media`, nuevo, `StateFlow` aparte)

| Campo | Tipo | Regla |
|---|---|---|
| `upNext` | `List<QueueEntry>` | Canciones que siguen a la actual en el orden real de reproducción, sin dar la vuelta. |
| `order` | `IntArray` | Orden completo de reproducción (índices de la lista de Media3). Con aleatorio, el orden aleatorio; sin él, `0..n-1`. Se usa para mover y deshacer con aleatorio. |

Se recalcula solo cuando cambian la línea de tiempo, la canción actual o el aleatorio. Aparte de `PlayerState` para que una cola de 2 000 canciones no viaje con cada evento.

### QueueEntry

| Campo | Tipo | Regla |
|---|---|---|
| `song` | `Song` | |
| `index` | `Int` | Índice en la lista de Media3; clave estable de la fila mientras no cambie la cola. |

## Repeat

`OFF → ALL → ONE → OFF` al tocar. Se traduce a `Player.REPEAT_MODE_*`.

## SleepTimerState / SleepTimer (`core/media`, puro)

| Opción | Minutos | Comportamiento |
|---|---|---|
| `OFF` | — | Sin temporizador. Elegirla cancela. |
| `M15`, `M30`, `M45`, `H1` | 15, 30, 45, 60 | `endsAt = ahora + minutos`; al llegar, pausa y vuelve a `OFF`. |
| `END_OF_SONG` | — | Pausa al terminar la canción que suena; luego `OFF`. |

Estado publicado: `option`, `endsAtMs: Long?` (solo opciones por tiempo, en `elapsedRealtime`). Restante en la interfaz = `endsAtMs − ahora`, nunca negativo.

Transiciones: `OFF —iniciar(op)→ activo`; `activo —iniciar(OFF)→ OFF`; `activo —cumplido / fin de canción→ OFF`; `activo —iniciar(otra)→ activo con la nueva`.

## Deshacer (interfaz, transitorio)

`Removed(item: MediaItem, index: Int, orderBefore: IntArray?)`: lo último quitado mientras el aviso está visible. Un nuevo «quitar» lo reemplaza. Deshacer reinserta en `index` y, si había aleatorio y el tamaño coincide, reenvía `orderBefore`.

## CoverColors (`feature/player`, ampliado)

Mismos campos (`container`, `accent`, `onAccent`, `content`, `contentVariant`, `track`); ahora con variante clara (research §9) y animados entre canciones.

## Estado de la capa (interfaz)

- `expansion: Float` 0..1 (tarjeta ↔ Reproduciendo), guardado su destino con `rememberSaveable`.
- `queueOpen: Boolean`, `timerSheetOpen: Boolean` (`rememberSaveable`).
- `dragPreview`: orden temporal mientras se arrastra una fila de la cola.
