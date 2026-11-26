# Tarea 09 · Letras

**Estado:** lista
**Escrita:** 2026-11-26 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.13.0 · **Spec Kit:** Parte A, arreglos pequeños (sin spec nueva; el símbolo con `/speckit-converge` sobre `specs/006-inicio-y-buscar/`); Parte B, función nueva en `specs/008-letras/`.
**Ponytail:** full en la Parte A; off del Paso B1 al B5; full desde el Paso B6.
**Dispositivo:** emulador; si el Redmi aparece en adb, instálale `perfil` al final.

## Respuestas a la tarea 08
- Decisiones 1–8: **aprobadas** (sin `SettingsViewModel`; texto de SCR-12; títulos «Términos» y «Privacidad»; Palette y Reorderable en Licencias; hoja del tema sin «Listo»; fila del temporizador; escaneo desde Ajustes; estilos del tema). `allowBackup`: **aprobado** (revisé las reglas de extracción).
- Figma ya está al día: Licencias con Palette y Reorderable, SCR-12 con el texto nuevo, Reproduciendo con portada **plana** en claro (DEC-27) y el mosaico de Inicio dice **«Favoritas · N canciones»** (así quedó en la app). **El mosaico «Letras» no existe en Inicio:** las letras guardadas se ven en Ajustes.
- Ajustes e hijas de Inicio **sí muestran la píldora** (se queda así).
- «Pendiente de revisar» de la tarea 08: la fila Tema que puede mostrar «Oscuro» un cuadro al entrar. Si lo ves en el Redmi, avisa.
- Falta, y es de Brian: probar `perfil` en el Redmi (fundido del tema, deslizar la tarea, «Escanear de nuevo» con un archivo nuevo y fluidez con 2 000 canciones).

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 09`. Si no, detente.

---
## Parte A · Arreglos (Ponytail full)
1. **Símbolo de Rola en tema claro** (Inicio y SCR-12): la «r» sale blanca y casi no se ve; en Figma (51:2090) es oscura. Dibuja el símbolo en dos capas para pintar la «r» con `onSurface` y el punto de acento con el color de marca. `/speckit-converge` sobre 006 si cambia algún requisito. Commit `fix(home): símbolo de Rola legible en claro`.
2. **Dos pruebas de `PlaybackStoreTest` fallan en esta máquina** (`Unable to rename …preferences_pb.tmp`). Averigua si es el entorno (carpeta de pruebas dentro del proyecto con antivirus o bloqueo de Windows) y, si lo es, usa una carpeta temporal del sistema. Si es una carrera real del código, avísame con la causa. Commit `test(media): carpeta temporal fuera del proyecto` (o el que corresponda).

---
## Parte B · Letras
### Lectura obligatoria
- Grafo: DEC-10 (fuentes), DEC-11 (coincidencia y desfase), DEC-12 (LRCLIB apagado), DEC-13, DEC-15, DEC-24 (tocar la carátula), DEC-23, DEC-09, DEC-27 (relieve solo en SCR-63), DEC-28, DEC-08 (borrar canción borra su letra), TEC-LRCLIB, TBL-LYRICS, KV-DATASTORE, OP-10 a OP-13, PEND-04 (**no entra**).
- Pantallas (oscuro / claro): SCR-60 Letras 38:1574 / 51:4780, SCR-61 Opciones de letra 38:1690 / 51:4894, SCR-62 Cambiar letra 38:1771 / 51:5014, SCR-63 Sin letra 38:1634 / 51:4840, SCR-71 Letras descargadas 40:1971 / 51:5439, SCR-75 Ajustes con LRCLIB resaltado 40:1829 / 51:5284, Reproduciendo 36:1465 / 51:4482. Usa `figma-design-to-code`.
- **Privacidad:** lo único que sale del teléfono es título, artista, álbum y duración hacia LRCLIB, y solo si el usuario lo activó. No se agrega ninguna otra conexión ni librería de red sin preguntar a Brian.
### Paso B1 · `/speckit-specify`
> Al tocar la carátula de Reproduciendo (o «Letra») veo la letra de la canción. Si tiene tiempos, la línea actual se resalta y la lista avanza sola; si no, la leo como texto. Si no hay letra, me ofrece buscarla en el teléfono (carpeta con archivos .lrc) o en internet (LRCLIB), si lo activé. Puedo cambiar de versión cuando la duración no coincide y ajustar el desfase de 0.5 en 0.5 s. En Ajustes activo o apago la búsqueda en internet, elijo la carpeta de letras y veo y borro las letras guardadas.
### Paso B2 · `/speckit-clarify` (propuestas; confirma lo dudoso con Brian)
- **Orden de fuentes (DEC-10):** (1) letra guardada en la app, (2) `.lrc` con el mismo nombre del audio en la carpeta elegida, (3) LRCLIB si está activado. Lo encontrado en (2) y (3) se guarda en la app para que funcione sin permiso ni red después.
- **Al abrir:** busca (1) y (2) sin red; si hay letra → SCR-60. Si no → SCR-61 (opciones «En el teléfono» y «En internet»). Si se intentó y no hubo resultado → SCR-63 con «Elegir carpeta» y «Buscar en internet».
- **LRCLIB apagado:** «Buscar en internet» lleva a Ajustes y SCR-75 resalta la fila (borde de acento que pulsa; **fija, sin animación**, con «reducir movimiento»). Al volver con LRCLIB ya encendido, lanza la búsqueda una vez. Por defecto **apagado** (`lrclib_enabled`).
- **Archivo `.lrc`:** etiquetas `[mm:ss.xx]` (varias por línea), se ignoran las de metadatos, se respeta `[offset:±ms]`; sin etiquetas = texto plano.
- **Línea activa:** por posición + desfase; resalta con el color del tema y se desplaza suave (≤ 300 ms, DEC-28; con «reducir movimiento», sin desplazamiento animado). Si el usuario desplaza a mano, el seguimiento se pausa y vuelve a los 4 s sin tocar. Tocar una línea **no** salta la canción.
- **Desfase:** −0.5 / +0.5 s, tope ±10 s, se guarda por canción (`offset_ms`) y se muestra como «DESFASE +0.5 s».
- **LRCLIB (TEC-LRCLIB):** HTTPS, `User-Agent: Rola/<versión>` (sin correo ni datos personales), tiempo de espera ~10 s. Primero `/api/get` con duración; si falla, `/api/search`. Limpieza de título como DEC-11. Errores: sin conexión → aviso con «Reintentar»; sin resultado → SCR-63. Se prefiere `HttpURLConnection` + `org.json` (cero dependencias nuevas).
- **«Cambiar letra» (SCR-62):** aparece si la duración difiere más de 2 s (DEC-11); lista de `/api/search` con tipo (Sincronizada / Solo texto), duración y diferencia («Igual», «+5 s»); «Usar esta letra».
- **Carpeta de letras:** selector del sistema (`OpenDocumentTree`) con ruta inicial sugerida (SnapTube Audio, Music, Download), permiso persistente, `lyrics_folder_uri` en DataStore; en Ajustes la fila muestra su nombre.
- **Letras descargadas (SCR-71):** todas las letras guardadas, orden A–Z por título, «N letras · X KB». Quitar una: deslizar con «Deshacer»; «Borrar todas»: diálogo de confirmación y aviso. Borrar una canción del dispositivo borra su letra (DEC-08).
- **Reproduciendo:** el botón «Letra» de Figma abre lo mismo que tocar la carátula. Si Figma dibuja otros botones sin acción definida en el grafo (por ejemplo ⋮), pregunta a Brian.
- **Inicio:** sin mosaico de Letras.
- **Color:** Letras toma el color de la carátula (PEND-08 opción A) con contraste asegurado; relieve solo en SCR-63 (DEC-27).
### Paso B3 · `/speckit-plan`
- `core/database`: tabla `lyrics` (media_id, source local|lrclib, synced, content, offset_ms, lrclib_id, duration_s, saved_at) con migración automática v2→v3 y prueba de migración.
- `core/lyrics` (parser LRC, limpieza de título, cliente LRCLIB tras una interfaz, repositorio) y `feature/lyrics`; `feature/settings` suma la sección LETRAS, «Carpeta de letras» y SCR-71. Permiso `INTERNET` (primera vez que hace falta).
- Movimiento (`animate`): entrada de Letras desde Reproduciendo con la transición habitual (≤ 300 ms), cambio de línea suave, hoja SCR-62 y SCR-61 con las animaciones de hojas de siempre.
### Paso B4–B5 · `/speckit-tasks` y `/speckit-analyze`
- Commit `docs(spec): 008 letras`. Detente y pide a Brian que pase Ponytail a **full**.
### Paso B6 · `/speckit-implement`
- Pruebas JUnit: parser LRC (varias etiquetas, metadatos, `[offset:]`, texto plano, líneas vacías), línea activa con desfase, limpieza de títulos, tolerancia de 2 s, orden de resultados de búsqueda, interpretación de las respuestas de LRCLIB con respuestas **inventadas** (no copies letras reales en pruebas ni en el repositorio). Migración v2→v3. Compose: SCR-60 con y sin tiempos, SCR-61, SCR-62, SCR-63, SCR-71, filas nuevas de Ajustes, SCR-75 resaltado.
- Prueba manual con red en el emulador: LRCLIB encendido, una canción con etiquetas reales (título y artista de una canción conocida) → letra guardada → sin red vuelve a mostrarse. Sin red: aviso y «Reintentar». Carpeta con un `.lrc` de prueba inventado.
- `design-critique` de SCR-60, 61, 62, 63, 71 y las filas nuevas de Ajustes, oscuro y claro.

---
## Paso final · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y todas las de Compose tras el último cambio.
- Capturas: `t09-letras.png`, `t09-letras-claro.png`, `t09-opciones-letra.png`, `t09-cambiar-letra.png`, `t09-sin-letra.png`, `t09-letras-descargadas.png`, `t09-ajustes-letras.png`, `t09-ajustes-resaltado.png`, `t09-simbolo-claro.png`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
