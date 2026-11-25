# Modelo de datos: Inicio y Buscar

## Room `rola.db` versión 2

Migración: `AutoMigration(from = 1, to = 2)`, solo agrega tablas. `favorites`, `playlists` y `playlist_songs` no cambian (FR-046).

### play_history (TBL-HISTORY)

| Columna | Tipo | Regla |
|---|---|---|
| id | INTEGER, clave autogenerada | |
| media_id | INTEGER | `Song.id` |
| played_at | INTEGER | reloj de pared en ms, cuando contó |

Índice: (`media_id`, `played_at`). Se borra por `media_id` tras un borrado del dispositivo (DEC-08).

Consulta agrupada → `PlayStats(mediaId, plays, lastPlayedAt)`.

### playback_queue (TBL-QUEUE)

| Columna | Tipo | Regla |
|---|---|---|
| position | INTEGER, clave | lugar en la lista de Media3, desde 0, sin huecos |
| media_id | INTEGER | `Song.id`; puede repetirse |
| play_order | INTEGER | lugar en el orden en que suena; igual a `position` sin aleatorio (confirmada por Brian, plan · Seguimiento) |

Se reemplaza completa en una transacción. Vacía = sin cola.

## DataStore `rola_reproduccion`

| Clave | Tipo | Por defecto |
|---|---|---|
| indice | Int | 0 (lugar en `position`) |
| minuto_ms | Long | 0 |
| aleatorio | Boolean | false |
| repetir | String (`OFF`, `ALL`, `ONE`) | `OFF`; otro valor = `OFF` |
| origen | String? | nulo = Biblioteca |

El temporizador no se guarda (FR-029).

## Tipos en memoria

### SavedPlayback

| Campo | Tipo | Nota |
|---|---|---|
| ids | List<Long> | en orden de Media3 |
| order | List<Int> | permutación de índices en orden de reproducción |
| index | Int | dentro de `ids` |
| positionMs | Long | ≥ 0 |
| shuffle | Boolean | |
| repeat | Repeat | |
| source | String? | |

Reglas:
- `index` fuera de rango → se recorta.
- `order` que no es permutación de `ids.indices` (guardado dañado) → orden de la lista.
- `keepExisting(existe)`: quita los ids que no existen, renumera `order` y `index`. Si faltaba el actual: el siguiente existente según `order`, o el primero de `order`, con `positionMs = 0`. Sin ids → `null`.

### PlayCounter

Estados de una escucha: *sin empezar* → *sumando* (suena) ⇄ *en pausa* → *contada*. Cambia de canción → empieza otra escucha.

### Inicio

- `HomeState`: `Loading` | `Empty` (sin canciones) | `Content(recent, mostPlayed, songCount, favoriteCount)`; `recent` vacío = SCR-11.
- `MostPlayed(song, plays)`.

### Buscar

- `Match(start, end)`: tramo del texto original.
- `SearchResults(query, songs, albums, artists, playlists)`: hasta 5 por grupo, cada elemento con su `Match`. `null` = sin texto.
