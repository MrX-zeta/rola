# Tarea 05 · Favoritas y Listas (Room)

**Estado:** esperando revisión
**Escrita:** 2026-11-15 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.8.0 · **Spec Kit:** `specs/004-favoritas-y-listas/`.
**Ponytail:** off del Paso 1 al Paso 5; full del Paso 6 en adelante.
**Dispositivo:** emulador; si el Redmi aparece en adb, instálale `perfil` al final.

## Objetivo
Marco canciones como favoritas con un corazón animado sobre la portada, las encuentro en «Favoritas», creo mis propias listas, les agrego canciones y uso el menú de opciones de cada canción (cola, listas, favorita, borrar del dispositivo).

## Lectura obligatoria
- Grafo: DEC-29 (Favoritas), DEC-06 (listas), DEC-07 (menú), DEC-08 e INV-02 (borrar), DEC-05 (abecedario en la hoja de agregar), DEC-09, DEC-27, DEC-28, TBL-FAVORITES, TBL-PLAYLISTS, TBL-PLAYLIST-SONGS, OP-04 a OP-08.
- Pantallas: SCR-51 (botón de favorita: Figma 36:1465 / 51:4482), SCR-23, SCR-30, SCR-31, SCR-32, SCR-33, SCR-54 (y sus `figma_nodo_claro`). Usa `figma-design-to-code`.

## Respuestas a la tarea 04
- KV-DATASTORE pasa a decidido. Instalaciones previas que ven la bienvenida una vez: está bien así.
- Emulador: sí, borra los archivos viejos de `/sdcard` (`s*.raw`, `rec_nav*.mp4`).

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 05`. Si no, detente.

## Paso 1 · `/speckit-specify`
> Quiero marcar como favorita la canción que suena tocando un corazón sobre su portada, con una animación que se sienta bien, y encontrarlas todas en «Favoritas», fija arriba en Biblioteca · Listas. También quiero crear listas con un nombre, agregarles canciones desde una hoja con buscador, abecedario y casillas, reproducirlas o en aleatorio, quitar canciones, y desde el menú ⋮ (o pulsación larga) de cualquier canción: añadir a la cola, añadir a una lista, marcar/quitar favorita y borrar del dispositivo con el aviso de Android.

## Paso 2 · `/speckit-clarify`
- Favoritas es una lista automática (no se renombra ni se borra), ordenada por fecha de marcado, más reciente arriba.
- Biblioteca muestra ahora los chips «Listas» y «Canciones» (Álbumes y Artistas llegan en la tarea 06; no los muestres).
- Borrar del dispositivo: `MediaStore.createDeleteRequest`, sin confirmación propia; al terminar, snackbar y limpieza de favoritas, listas y cola (DEC-08, INV-02).
- Las referencias a canciones se guardan por id del MediaStore; si una canción ya no existe, se omite al mostrar (sin borrar la fila hasta que se confirme que no existe).
- Lista vacía (SCR-31) y Lista con canciones (SCR-32) según DEC-06; en una lista el menú lleva «Quitar de la lista» y «Añadir a la cola».
- Fuera: renombrar/borrar listas, reordenar canciones dentro de una lista, Álbumes/Artistas (tarea 06), historial (tarea 07).

## Paso 3 · `/speckit-plan`
- `core/database` con Room (KSP): `favorites`, `playlists`, `playlist_songs`, con esquema exportado y pruebas de DAO. Dependencias permitidas: Room (runtime, ktx, compiler) y el plugin KSP.
- `feature/playlists` (listas), `feature/player` (corazón), menú compartido de canción.
- **Corazón (skill `animate`, DEC-29):** al marcar, relleno lima con rebote de escala (resorte), ráfaga corta de 6–8 partículas lima que salen del corazón y se desvanecen, y háptica de confirmación; ≤ 350 ms. Al desmarcar, vuelve al contorno sin ráfaga. Interrumpible si se toca rápido varias veces. Con reducir movimiento: solo cambio de relleno. Accesible como interruptor «Favorita».
- Hoja de agregar canciones: `ModalBottomSheet` con buscador, abecedario (DEC-05) y casillas; botón «Agregar N canciones».

## Paso 4 · `/speckit-tasks` · Paso 5 · `/speckit-analyze`
- Commit `docs(spec): 004 favoritas y listas`. Detente y pide a Brian que pase Ponytail a **full**.

## Paso 6 · `/speckit-implement`
- Commits atómicos por área (`feat(database): …`, `feat(player): corazón de favoritas`, `feat(playlists): …`, `feat(library): menú de opciones`).
- Pruebas: DAO (insertar, quitar, orden, borrar en cascada), repositorio de favoritas, normalización del buscador; Compose: corazón cambia de estado y con reducir movimiento no escala, Favoritas fija arriba, lista vacía con su botón, menú con sus opciones según el lugar.
- `design-critique` de SCR-23, SCR-30 a SCR-33, SCR-51 (corazón) y SCR-54.

## Paso 7 · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- En emulador: marcar/desmarcar favorita, Favoritas en Listas, crear lista, agregar canciones, reproducir, quitar, añadir a la cola, borrar del dispositivo (archivo de prueba) y que desaparezca de favoritas y listas.
- Capturas: `t05-favorita.png`, `t05-listas.png`, `t05-agregar.png`, `t05-menu.png`; video `t05-corazon.mp4`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
