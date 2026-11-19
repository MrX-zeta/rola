# Reporte · Tarea 05 · Favoritas y Listas (Room)

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Worktree actualizado a `origin/development` (avance rápido). `git log` incluía `docs: sesión de diseño tarea 05`. ✅
- Skills disponibles: Spec Kit, `animate`, `figma-design-to-code`, `design-critique`, `ponytail-review`. No faltó ninguna.
- Ponytail: off en los pasos 1–5 y full desde el 6, con el cambio hecho por Brian.

## Pasos 1–5 · Spec Kit (`specs/004-favoritas-y-listas/`)
- Spec con 6 historias y 37 requisitos. Las respuestas del Paso 2 quedaron en «Aclaraciones»; no hubo preguntas a Brian.
- Plan, research, data-model, contratos, quickstart y 51 tareas.
- `/speckit-analyze`: sin problemas críticos. Se aplicaron 3 arreglos menores aprobados por Brian: Favoritas es la primera lista debajo de «Nueva lista», las canciones se agregan en el orden de la hoja, y se agregó la prueba de 2 000 canciones.
- Commit `docs(spec): 004 favoritas y listas`.

## Paso 6 · Implementación
| Commit | Qué |
|---|---|
| `feat(database): favoritas y listas con Room` | Room 2.8.5 + KSP 2.3.14; tablas `favorites`, `playlists`, `playlist_songs` (cascada, índice en `media_id`); esquema v1 en `app/schemas/`; pruebas de DAO |
| `feat(data): repositorio de favoritas y listas, buscador y cola` | `LibraryRepository` (oculta las canciones que ya no existen sin borrar su fila), `SearchText`, `PlaylistName`, una sola consulta al MediaStore compartida, `addToQueue` (al final también con aleatorio) y `removeSong` |
| `feat(player): corazón de favoritas` | `FavoriteButton` sobre la portada (DEC-29) |
| `feat(playlists): listas, Favoritas, hoja para agregar y menú de opciones` | Chips Listas/Canciones, `PlaylistsTab`, `CreatePlaylistDialog`, `PlaylistScreen` (vacía, con canciones, Favoritas), `AddSongsSheet` + `AlphabetBar`, `SongMenu` (⋮ y pulsación larga), «Añadir a una lista», borrar con `createDeleteRequest` |
| `fix(player): orden de parámetros de Reproduciendo (lint)` | Aviso de lint que había introducido yo |
| `test(player): la prueba del corazón mide dentro de su capa` | Arreglo de la prueba (ver abajo) |

**El corazón (skill `animate`).** Se usa decenas de veces al día y su fin es confirmar el toque, con un toque de deleite que pide DEC-29.
- Al marcar: el ícono pasa a relleno lima al instante, rebota desde 0.8 con un resorte (amortiguación 0.5, rigidez 800, se asienta en unos 330 ms), sueltan 7 partículas lima en 300 ms con la curva `EaseOut` de `RolaMotion` y hay vibración `Confirm`.
- Al desmarcar: contorno, sin ráfaga.
- Solo se anima al tocarlo, no cuando cambia la canción. Cada toque retoma la animación desde donde iba, así que no se encolan.
- Con reducir movimiento solo cambia el relleno.
- Para el lector de pantalla es un interruptor «Favorita» con área táctil de 48 dp.
- La ráfaga se lee en la fase de dibujo, sin recomponer.
- Falta juzgar la sensación en el Redmi con `perfil`.

## Decisiones que tomé (por favor revisa)
1. **«Favorita» en el menú (SCR-54).** El nodo 35:1458 no la tiene porque es anterior a DEC-29. La agregué como tercera opción en Biblioteca, con el ícono de corazón. En una lista el menú lleva solo «Quitar de la lista» y «Añadir a la cola», como pidió la tarea.
2. **Contorno del corazón.** En Figma solo existe `icon/favorite_filled` (78:3332). Para el estado apagado usé el `favorite` de contorno de Material Symbols, con el mismo trazo.
3. **«Añadir a una lista» no tiene pantalla en Figma.** Hice una hoja inferior con «Nueva lista» arriba y las filas de Biblioteca · Listas, sin Favoritas. Si eliges «Nueva lista», se crea y la canción se agrega.
4. **Favoritas** usa la pantalla de lista sin el botón Agregar. Su estado vacío dice «Aún no tienes favoritas» y no tiene botón. «Quitar de la lista» en Favoritas quita la favorita y se puede deshacer, conservando su fecha.
5. **Biblioteca abre en el chip «Canciones»**, como antes. Figma muestra «Listas» primero; si lo prefieres, es un cambio de una línea.
6. **Orden de las listas:** la más nueva arriba, debajo de Favoritas. No estaba definido.
7. **Una lista que desaparece por fuera de Rola** (otra app o una SD retirada): sus canciones se ocultan y su fila se conserva. Las filas solo se borran cuando Rola confirma un borrado propio.
8. **Sin `PlaylistViewModel`.** El detalle se lee directo del repositorio con `collectAsStateWithLifecycle`, que es menos código. Las acciones compartidas viven en `LibraryViewModel`.
9. **Nuevos tokens:** `RolaColors.overCover` / `onOverCover` para el círculo translúcido del corazón (Figma `rgba(17,17,17,.7)`) y `Shapes.tile` (16 dp) para los mosaicos y el menú.
10. **Esquema exportado con el argumento de KSP** `room.schemaLocation`, sin el plugin de Gradle de Room, porque la tarea no lo permitía.

## Dudas para la sesión de diseño
- **INV-06 y el abecedario:** las letras miden 17 dp de alto (Figma). Para el lector de pantalla son un solo control «Abecedario» con una acción por letra, y el ancho táctil es de 32 dp para no tapar las casillas. ¿Se acepta como excepción, o se propone en el grafo?
- **Fuera de alcance según la tarea, pero visible en Figma:**
  - el ⋮ de cada lista en Listas (32:1128) y el de la pantalla de lista (34:1345), que servirían para renombrar o borrar;
  - el botón «Añadir a una lista» junto al título y el ⋮ en Reproduciendo (36:1465).

  No los agregué. ¿Van en otra tarea?
- **«Reproduciendo desde» sigue diciendo «Biblioteca»** aunque suene una lista. ¿Debe decir el nombre de la lista?
- **Tema claro:** la app tiene `darkTheme = true` fijo (ya era así antes). Las pantallas nuevas usan solo tokens del tema, pero en la app no se pudo ver el modo claro.
- **Lo que Figma no pidió:** al crear una lista no sale un aviso, porque se abre la lista misma. Al cerrar la hoja sin confirmar tampoco hay aviso.

## design-critique (capturas del emulador contra Figma, oscuro)
| Pantalla | Diferencias |
|---|---|
| SCR-51 corazón | Coincide en posición (14 dp desde la esquina), tamaño (48 dp) y colores. Faltan «Letra», «Añadir a una lista» y ⋮, de otras tareas. La portada es genérica porque los audios de prueba no tienen carátula. |
| SCR-23 Listas | Solo 2 chips (por la tarea). Sin ⋮ en las filas. Los mosaicos salen con un solo recuadro porque los audios de prueba no tienen carátula; con 4 o más canciones con carátula se arma el 2×2. |
| SCR-30 Crear lista | El campo usa `OutlinedTextField` de M3 (etiqueta sobre el borde). En Figma la etiqueta va dentro del recuadro. Lo demás coincide. |
| SCR-31/32 Lista | Coincide (portada 112 dp radio 24, título 28, Reproducir lima, círculos de 48). Falta el ⋮ de la lista (fuera de alcance). |
| SCR-33 Hoja | Coincide (buscador, casillas lima de 22 dp, abecedario, botón «Agregar N canciones»). Diferencias: la hoja llega hasta arriba (Figma deja unos 77 dp de velo); las letras sin canciones salen atenuadas (no está en Figma); el abecedario está centrado en vertical. |
| SCR-54 Menú | Coincide (fondo `surfaceContainer`, radio 16, divisor antes de Borrar), más la opción «Favorita». |
| Accesibilidad | Chips con toque de 48 dp, ⋮ «Opciones de X», casillas con su título, corazón como interruptor. El aviso (snackbar) tapa unos píxeles de la mini tarjeta; eso ya pasaba antes. |

## Paso 7 · Comprobaciones
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug`: **BUILD SUCCESSFUL**. Lint: 0 errores y 2 avisos que ya existían (`VectorRaster` en `splash_icono`, `ExportedService`).
- `connectedDebugAndroidTest` en el emulador: **53 pruebas**. La primera vez falló 1, la del rebote del corazón: la etiqueta de prueba estaba fuera del `graphicsLayer` y medía sin escala, y además miraba a los 50 ms. Lo corregí y las 3 del corazón pasan; no volví a correr las 53 completas tras el arreglo.
- **Recorrido en el emulador**, con 14 audios WAV de prueba generados por mí en `/sdcard/Music`:
  - marcar el corazón ✅
  - Favoritas con su conteo ✅
  - crear «Viaje» y abrirla con la hoja ✅
  - marcar 3 y agregar ✅
  - el abecedario salta ✅
  - menú en Canciones ✅
  - «Añadir a una lista» y luego «Ya estaba» ✅
  - «Añadir a la cola» ✅
  - «Borrar del dispositivo»: solo el aviso de Android y luego «Se borró…»; desapareció de Canciones (14→13), Favoritas (3→2), «Viaje» (4→3) y de la cola, donde estaba dos veces ✅
  - «Quitar de la lista» y Deshacer, que vuelve a su lugar ✅
- **SC-007 (2 000 canciones):** cargué 2 000 audios de 1 s y abrí la hoja en `perfil`. Funciona: busca, salta de letra y desplaza. Pero el emulador dibuja por software (`ro.hardware.egl=emulation`, GPU de 4,9 s por cuadro): hasta Biblioteca, que no cambió, da 97 % de cuadros lentos. **La fluidez no se puede juzgar aquí; falta probarla en el Redmi.** Los archivos de carga ya se borraron.
- **Redmi:** estaba conectado al principio, pero al final ya no aparecía en adb, así que **no se instaló `perfil`**.
- Emulador: borré `s*.raw` y `rec_nav*.mp4` de `/sdcard`, como autorizó la tarea. Quedan los 13 WAV de prueba en `/sdcard/Music`.
- No reinicié el emulador colgado yo mismo (el permiso lo bloqueó); lo reinició Brian.

## Capturas (`ai/handoff/capturas/`)
`t05-favorita.png`, `t05-listas.png`, `t05-agregar.png`, `t05-menu.png`, y además `t05-crear.png`, `t05-lista.png` y `t05-borrar-aviso.png`.
**Sin video `t05-corazon.mp4`:** la regla del proyecto dice que bastan las capturas. Si hace falta el video, se graba aparte.

## Ponytail
`/ponytail:ponytail-review` antes de cada commit. Encontró y se corrigió:
1. «Añadir a la cola» con aleatorio caía en un lugar al azar; ahora va al final.
2. Un commit no compilaba sin `strings.xml`; se incluyó.

## Git
Rama del worktree subida a `origin development` (avance rápido). Sin autoría de IA en los mensajes.
