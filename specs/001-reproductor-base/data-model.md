# Modelo de datos: Reproductor base

Todo vive en memoria; nada se guarda en disco (spec FR-018b).

## Song (`core/data`)

| Campo | Tipo | Regla |
|---|---|---|
| `id` | `Long` | `_ID` del MediaStore; único. |
| `uri` | `Uri` | `ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, id)`. |
| `title` | `String` | `TITLE`; si está vacío, `DISPLAY_NAME` sin extensión. Nunca vacío. |
| `artist` | `String?` | `ARTIST`; `null` si está vacío o es `<unknown>`. La interfaz muestra «Artista desconocido». |
| `album` | `String?` | `ALBUM`; mismas reglas que artista. Solo para metadatos de la notificación. |
| `durationMs` | `Long` | `DURATION`; ≥ 0. |

Orden: `TitleOrder.comparator` (DEC-05). Ver [research.md](research.md) §6.

## PlayerState (`core/media`)

| Campo | Tipo | Regla |
|---|---|---|
| `current` | `Song?` | `null` = no hay nada cargado → la mini tarjeta no se dibuja (FR-019). |
| `isPlaying` | `Boolean` | |
| `positionMs` | `Long` | Se refresca cada ~500 ms mientras suena. |
| `durationMs` | `Long` | De la canción actual; 0 si es desconocida. |
| `hasNext` | `Boolean` | Habilita el botón siguiente. |

Transiciones:

```text
vacío ──play(lista, i)──▶ sonando ◀──toggle──▶ en pausa
sonando ──fin de la última canción──▶ en pausa (misma canción, FR-018a)
sonando ──error de archivo──▶ siguiente canción + aviso (FR-018)
cualquiera ──app cerrada y servicio detenido──▶ vacío (FR-018b)
```

## MusicPermissionState (`feature/onboarding`)

`Granted` · `NotAsked` · `Denied`

```text
NotAsked ──Permitir acceso + aceptar──▶ Granted
NotAsked ──Permitir acceso + negar──▶ Denied
Denied ──Intentar de nuevo + aceptar / concedido en Ajustes y ON_RESUME──▶ Granted
Granted ──retirado en Ajustes y ON_RESUME──▶ NotAsked
```

## CoverColors (`feature/player`)

| Campo | Tipo | Regla |
|---|---|---|
| `container` | `Color` | Fondo de la tarjeta: swatch oscuro de Palette o `surfaceContainer`. |
| `accent` | `Color` | Botón reproducir y línea de progreso: swatch vivo o `accent` del tema. |
| `onAccent` | `Color` | Ícono sobre el acento; contraste ≥ 4.5:1. |
| `content` | `Color` | Título; contraste ≥ 4.5:1 sobre `container`. |
| `contentVariant` | `Color` | Artista; contraste ≥ 4.5:1 sobre `container`. |

Todos los pares texto/fondo pasan por `ensureContrast` (FR-022).
