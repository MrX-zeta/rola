# Modelo de datos: Favoritas y Listas

Base de datos Room `rola.db`, versión 1, esquema exportado en `app/schemas/`. Tablas del grafo: TBL-FAVORITES, TBL-PLAYLISTS, TBL-PLAYLIST-SONGS. Las canciones no se copian: se guarda `media_id` = `MediaStore.Audio.Media._ID` (`Song.id`).

## `favorites` (TBL-FAVORITES)

| Columna | Tipo | Reglas |
|---|---|---|
| `media_id` | INTEGER | Clave primaria. Una canción es favorita una sola vez. |
| `added_at` | INTEGER | Milisegundos desde epoch al marcar. Orden de Favoritas: `added_at DESC`. |

- Marcar = `INSERT OR IGNORE`; desmarcar = `DELETE WHERE media_id = ?`.

## `playlists` (TBL-PLAYLISTS)

| Columna | Tipo | Reglas |
|---|---|---|
| `id` | INTEGER | Clave primaria autoincremental. |
| `name` | TEXT | Recortado; 1–60 caracteres; puede repetirse entre listas. |
| `created_at` | INTEGER | Al crear. |
| `updated_at` | INTEGER | Al crear y cada vez que se agregan o quitan canciones. |

- Orden en Biblioteca · Listas: `created_at DESC` (la más nueva arriba, debajo de Favoritas). Se pregunta en el reporte.

## `playlist_songs` (TBL-PLAYLIST-SONGS)

| Columna | Tipo | Reglas |
|---|---|---|
| `playlist_id` | INTEGER | Clave foránea a `playlists.id`, `ON DELETE CASCADE`. |
| `media_id` | INTEGER | Índice propio (para limpiar al borrar del dispositivo). |
| `position` | INTEGER | Orden dentro de la lista. Agregar = `MAX(position) + 1` en adelante, en el orden de la hoja. |
| `added_at` | INTEGER | Al agregar. |

- Clave primaria compuesta `(playlist_id, media_id)`: una canción no se repite en la misma lista. Agregar = `INSERT OR IGNORE`.
- Quitar no renumera: el orden solo depende de `position` creciente. Deshacer reinserta la fila con su `position` y `added_at` de antes.

## Vistas que arman los repositorios (no son tablas)

- **Resumen de lista**: id, nombre, número de canciones existentes, ids de las 4 primeras existentes (portada). Favoritas tiene su propio resumen y va siempre primera.
- **Detalle de lista**: nombre, canciones existentes en orden, duración total.
- «Existente» = el `media_id` está en la lista actual del `SongRepository`.

## Transiciones

```text
canción ── marcar ──▶ favorita ── desmarcar ──▶ canción
lista: creada (vacía) ── agregar ──▶ con canciones ── quitar la última ──▶ vacía
borrado confirmado por Rola ──▶ DELETE favorites + playlist_songs WHERE media_id = ? ; quitar de la cola
canción desaparece por fuera ──▶ filas intactas, ocultas al mostrar
```
