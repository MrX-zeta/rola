# Tarea 08 · Ajustes y legal

**Estado:** lista
**Escrita:** 2026-11-26 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.12.0 · **Spec Kit:** función nueva en `specs/007-ajustes-y-legal/` (`/speckit-specify` → `-clarify` → `-plan` → `-tasks` → `-analyze` → `-implement`).
**Ponytail:** off del Paso 1 al 5; full desde el Paso 6.
**Dispositivo:** emulador; si el Redmi aparece en adb, instálale `perfil` al final.

## Respuestas a la tarea 07
- Decisiones 1–10: **aprobadas**. Anotadas en DEC-30 y DEC-25 (cola de Biblioteca con origen «Biblioteca», Buscar compara nombres, fecha en lugar de la hora de Figma, hijos de Inicio y de Buscar).
- `play_order` en `playback_queue`: **sí** (ya en TBL-QUEUE). **Temporizador de sueño: no se guarda**; quité `sleep_timer_end` del grafo.
- **Copia de seguridad de Android:** se **desactiva** (`allowBackup="false"`). Va en esta tarea (Paso 6, primer commit).
- SCR-12 dice «revisa qué carpetas se escanean»: en esta tarea aparece «Abrir Ajustes»; deja el texto verdadero con lo que Ajustes ofrece (Escanear medios; no hay selector de carpetas).
- Historial sin tope ni borrado (PEND-10) y reanudar desde audífonos con la app cerrada (PEND-11): **se posponen** (tarea 11).
- Falta, y es de Brian: deslizar la tarea en el Redmi y probar «Escanear de nuevo» con un archivo que el sistema no conozca.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye el commit de la sesión de diseño de la tarea 08 (`docs: sesión de diseño tarea 08`). Si no, detente.

---
## Lectura obligatoria
- Grafo: DEC-23 (Legal), DEC-26 (Escaneo), DEC-18 (nota de abajo), DEC-09, DEC-16 (tema claro), DEC-27, DEC-28, DEC-12, KV-DATASTORE, OP-16, PEND-07 (no entra), PEND-09.
- Pantallas: SCR-70 Ajustes (40:1674 / claro 51:5129), SCR-72 Términos (41:1778 / 51:5538), SCR-73 Privacidad (41:1810 / 51:5570), SCR-74 Licencias (41:1842 / 51:5602), Temporizador (36:1651 / 51:4662). Usa `figma-design-to-code`. Lee los textos legales **completos** en Figma (el aviso de privacidad ya no menciona Notificaciones).
- **Nota DEC-18:** «Otros avisos» son mensajes dentro de la app (como «Lista creada»), **no** notificaciones del sistema: no se pide `POST_NOTIFICATIONS`.

## Qué entra y qué no en Ajustes (DEC-30: no se muestra lo que aún no existe)
- **Sí:** REPRODUCCIÓN (Temporizador de apagado, Otros avisos), BIBLIOTECA (Escanear medios con «Último escaneo»), APARIENCIA (Tema), ACERCA DE (Términos, Privacidad, Licencias, Versión).
- **No todavía:** sección LETRAS y «Carpeta de letras» (tarea 09, con SCR-71 y SCR-75), «Agregar widget» (tarea 10).
- Se hacen visibles el **engrane de Inicio** y el botón **«Abrir Ajustes»** de SCR-12. Ajustes es hijo de la pestaña desde la que se abre (la píldora no salta; atrás regresa).

### Paso 1 · `/speckit-specify`
> En Ajustes cambio el tema (sistema, oscuro o claro) y se aplica al instante; programo el temporizador de apagado y veo cuánto falta; decido si veo mensajes informativos como «Lista creada»; escaneo mis medios y veo cuándo fue la última vez; leo los términos, el aviso de privacidad y las licencias; y veo la versión. Llego desde el engrane de Inicio o desde «Abrir Ajustes» cuando no hay canciones.
### Paso 2 · `/speckit-clarify` (propuestas; confirma lo dudoso con Brian)
- **Tema:** `theme` = sistema | oscuro | claro, **por defecto oscuro** (lo que ya se ve). Hoja de selección con tres opciones, mismo estilo que la hoja de ordenar (33:1070). Se aplica sin reiniciar, incluida la barra de estado; el ícono y la pantalla de inicio siguen al sistema.
- **Temporizador:** la fila abre la misma hoja del reproductor; muestra «Apagado» o el tiempo restante. No se guarda al cerrar la app.
- **Otros avisos** (`other_notifications_enabled`, por defecto encendido): apagado oculta solo los mensajes informativos sin acción (por ejemplo «Lista creada», «Agregada a la cola»). **Siempre se muestran:** los que traen «Deshacer», los errores y el resultado de acciones pedidas (escaneo). Los controles del reproductor siempre se muestran.
- **Escanear medios:** misma lógica que «Escanear de nuevo» de SCR-12 (OP-16), con progreso y resultado; guarda `last_scan_at` y muestra «Último escaneo: hace 2 días» en español (un momento, minutos, horas, días).
- **Versión:** sale de `BuildConfig`, no escrita a mano.
- **Legal:** textos en `strings.xml`; pantallas con desplazamiento, flecha atrás y encabezado con rol. Licencias: agrega Coil, Palette y Reorderable (Apache 2.0) a las de Figma; yo actualizo Figma al revisar.
- **Copia de seguridad:** `allowBackup="false"` (PEND-09).
### Paso 3 · `/speckit-plan`
- `feature/settings` (Ajustes y las tres pantallas legales) dentro del NavHost. Claves nuevas en el DataStore `rola_ajustes`: `theme`, `other_notifications_enabled`, `last_scan_at`.
- Tema: `RolaTheme` lee `theme` y lo aplica al instante; sin parpadeo al arrancar (leer antes de dibujar).
- «Otros avisos»: un solo punto por donde pasan los avisos informativos, no uno por pantalla.
- Movimiento (`animate`): sin animaciones nuevas salvo la ya definida en DEC-28; la hoja del tema y los interruptores usan la animación de siempre; cambio de tema con fundido ≤ 300 ms, sin saltos; respeta «reducir movimiento».
### Paso 4–5 · `/speckit-tasks` y `/speckit-analyze`
- Commit `docs(spec): 007 ajustes y legal`. Detente y pide a Brian que pase Ponytail a **full**.
### Paso 6 · `/speckit-implement`
- Primer commit de código: `allowBackup="false"` (`fix(app): desactivar la copia de seguridad`).
- Pruebas: JUnit de «hace X» en español, tema por defecto y guardado, filtro de «Otros avisos» (con y sin Deshacer); Compose: Ajustes muestra solo las filas permitidas, el tema cambia y persiste, los tres documentos se leen y se puede volver, engrane y «Abrir Ajustes» llevan a Ajustes y atrás regresa.
- **Tema claro por primera vez en el teléfono:** captura en claro de Inicio, Biblioteca (4 pestañas), Buscar, Reproduciendo, Cola, Temporizador, Ajustes y las tres legales, y compara con los nodos claros de Figma (51:…). Reporta cada diferencia. Nodos claros: Inicio 51:2090, Biblioteca Canciones 51:2359, Álbumes 51:2577, Artistas 51:2737, Listas 51:2902, Buscar 51:4081, Reproduciendo 51:4482, Cola 51:4570.
- `design-critique` de SCR-70, 72, 73, 74 en oscuro y en claro.

---
## Paso final · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y todas las de Compose tras el último cambio.
- Capturas: `t08-ajustes.png`, `t08-ajustes-claro.png`, `t08-tema.png`, `t08-terminos.png`, `t08-privacidad.png`, `t08-licencias.png`, `t08-inicio-claro.png`, `t08-reproduciendo-claro.png`.
- Pregunta a Brian solo lo que no esté claro. `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
