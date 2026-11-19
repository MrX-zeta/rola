# Modelo de datos: Biblioteca completa

Nada se guarda en Room. Álbumes y artistas se derivan de las canciones; solo el orden de Canciones se guarda en DataStore.

## Song (cambia)

| Campo | Tipo | Origen | Nota |
|---|---|---|---|
| id | Long | `_ID` | existente |
| title | String | `TITLE` / `DISPLAY_NAME` | existente |
| artist | String? | `ARTIST` | existente; nulo = desconocido |
| album | String? | `ALBUM` | existente; nulo = desconocido |
| durationMs | Long | `DURATION` | existente |
| **albumId** | Long | `ALBUM_ID` | nuevo, por defecto 0 |
| **albumArtist** | String? | `ALBUM_ARTIST` | nuevo; nulo si vacío o `<unknown>` |
| **track** | Int | `TRACK` | nuevo; 0 = sin número; incluye disco × 1000 |
| **year** | Int | `YEAR` | nuevo; 0 = sin año |

## Album (derivado)

| Campo | Tipo | Regla |
|---|---|---|
| id | Long | `albumId` |
| name | String? | `album` de sus canciones; nulo = «Álbum desconocido» en la interfaz |
| artist | String? | primer `albumArtist`, si no `artist` de la primera pista |
| year | Int | mayor `year` de sus canciones; 0 = sin año |
| songs | List<Song> | por `track` (0 al final), empate por título (DEC-05) |
| durationMs | Long | suma de `songs` |
| cover | Song | primera de `songs` (portada) |

Unicidad: un `Album` por `albumId`. Orden de la lista: `name` con DEC-05.

## Artist (derivado)

| Campo | Tipo | Regla |
|---|---|---|
| name | String? | `albumArtist ?: artist`; nulo = «Artista desconocido» (una sola entrada) |
| albums | List<Album> | álbumes con alguna canción suya, más reciente primero |
| songs | List<Song> | por álbum (orden de `albums`) y pista |
| cover | Song? | portada del álbum más reciente |

Unicidad: un `Artist` por `name` (comparado tal cual). Orden de la lista: `name` con DEC-05.

## SongSort (DataStore `sort_order`)

| Campo | Valores | Por defecto |
|---|---|---|
| by | `TITLE`, `ARTIST`, `ALBUM` | `TITLE` |
| ascending | `true` (A→Z), `false` (Z→A) | `true` |

Formato guardado: `"<BY>_<ASC|DESC>"`. Un valor que no se reconoce o un archivo ilegible = `TITLE_ASC`.
