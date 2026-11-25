# Reporte · Tarea 07 · Inicio y Buscar

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Worktree actualizado a `origin/development` (avance rápido). `git log` incluía `docs: sesión de diseño tarea 07`. ✅
- Skills disponibles: Spec Kit, `figma-design-to-code`, `animate`, `design-critique`, `ponytail-review`. No faltó ninguna. Figma respondió bien.
- Ponytail: full en la Parte A, off del B1 al B5, full desde el B6. Brian hizo cada cambio.

## Parte A · Pendientes de Biblioteca
| Commit | Qué |
|---|---|
| `docs(spec): 005 pendientes` | «< 1 min», «se vuelve atrás», PEND-05 resuelto, FR-027 y FR-028; `/speckit-converge` agregó T037–T041 |
| `feat(library): opciones de álbum y artista` | ⋮ en los dos detalles con «Añadir a la cola» y «Añadir a una lista» (todas las canciones, al final también con aleatorio). SCR-55 muestra «Ventanas · 3 canciones» y el aviso dice cuántas se agregaron |

## Parte B · Inicio y Buscar (`specs/006-inicio-y-buscar/`)
- **B1–B5:** spec (4 historias, 45 requisitos y 7 criterios de éxito), plan, research, data-model, contratos, quickstart y 47 tareas. `/speckit-analyze`: sin problemas críticos.
- **B6:**

| Commit | Qué |
|---|---|
| `feat(database): historial y cola con migración` | `play_history` y `playback_queue`; una sola `AutoMigration` v1→v2; borrar una canción del dispositivo borra su historial |
| `feat(media): la reproducción se conserva al cerrar` | `PlaybackStore` (cola en Room, valores sueltos en el DataStore `rola_reproduccion`), guardado fuera del hilo principal y restauración en pausa. La interfaz retiene sus órdenes hasta que el servicio termina de restaurar |
| `feat(home): Inicio con historial, primera vez y escaneo` | `PlayCounter` (regla de 30 s), historial, SCR-10/11/12, «Escanear de nuevo», la app abre en Inicio y la píldora sigue a la pestaña dueña de la pila |
| `feat(search): buscador con resultados agrupados` | SCR-40/41, resaltado sobre el texto original, hasta 5 por grupo |
| `fix(media): guardar también al terminar la cola` y `fix(home): color en claro y recortes` | Lo que encontraron la prueba manual y la revisión visual |

## Respuestas de Brian durante la tarea
- **Columna `play_order` en `playback_queue`: sí.** TBL-QUEUE solo lista `position` y `media_id`. Con aleatorio hay dos órdenes; sin la columna, al apagar aleatorio después de reabrir no volvería el orden del álbum. **Propuesta para el grafo:** agregarla a TBL-QUEUE.
- **Temporizador de sueño: preguntado dos veces, sin respuesta.** No se guarda. **El grafo se contradice:** DEC-31 propone «no» y KV-DATASTORE lista `sleep_timer_end`. Falta decidir y quitar uno.

## Decisiones que tomé (por favor revisa)
1. **Cola al tocar una canción en Inicio o Buscar:** la de Biblioteca · Canciones desde esa canción, con origen «Biblioteca». El grafo no lo define y DEC-24 no trae otro origen.
2. **Buscar compara nombres,** no el artista ni el álbum de cada canción. Así está SCR-40: con «mar», Marea Alta sale como artista y sus canciones no.
3. **Grupo Listas:** Figma no lo dibuja; usa la fila de Álbumes con chevron. El ⋮ va solo en canciones (DEC-07).
4. **Fecha y mosaico:** Figma dice «Viernes, 9:30 p. m.» y «Letras»; DEC-30 pide fecha y «Favoritas». Seguí el grafo.
5. **Favoritas desde Inicio** es hija de Inicio: la píldora no salta y «atrás» regresa a Inicio. Lo mismo con los detalles abiertos desde Buscar. Tocar la pestaña activa vuelve a su raíz.
6. **Teclado:** al volver a Buscar con texto no se abre, para no tapar los resultados.
7. **Canción ilegible:** el código anterior llamaba `play()` al pasar a la siguiente. Con la restauración, la app habría sonado sola al abrir. Se quitó (si sonaba, sigue sonando) y tiene prueba.
8. **Segundo DataStore:** el minuto se guarda cada 5 s; en `rola_ajustes` reescribiría los ajustes cada vez.
9. **Arreglo fuera de la lista:** los avisos tapaban la píldora porque su alto se medía sin márgenes. Lo corregí (una línea en `RolaNavHost`) porque el aviso del escaneo caía encima y cortaba el final de Inicio.
10. **`t07-sin-resultados.png` dice «lluvia acida»** porque adb no escribe acentos; la app los acepta (hay pruebas).

## Riesgos y dudas para la sesión de diseño
- **SCR-12 dice «revisa qué carpetas se escanean»,** pero «Abrir Ajustes» no se muestra hasta la tarea 08. ¿Se cambia el texto mientras tanto?
- **Copia de seguridad de Android:** `allowBackup` está activo, así que `rola.db` y la cola viajan a otro teléfono. Ahí los ids del MediaStore son otros: Favoritas, listas, historial y cola podrían señalar canciones distintas. **REQUIERE REVISIÓN HUMANA.** Ya pasaba con Favoritas y listas. ¿Se excluye la base?
- **Canciones en una tarjeta SD desmontada:** al restaurar se omiten y la cola guardada queda sin ellas (así lo pide DEC-31).
- **Historial:** crece sin límite y no hay forma de borrarlo a mano.
- **Retomar desde audífonos o notificación con la app cerrada:** no está pedido y no se hizo.

## design-critique (oscuro, capturas del emulador contra Figma)
La app solo muestra el tema oscuro todavía. Comparé los cinco nodos claros por captura y encontré una diferencia: en claro el círculo de «Tu más escuchada» es negro. Ya sale de `onPrimaryContainer`/`background`.

| Pantalla | Resultado |
|---|---|
| SCR-10 Inicio | Coincide: saludo, carrusel de 112, destacado y bento. Diferencias a propósito: fecha, «Favoritas», sin «Ver todo» ni engrane. Tokens: saludo 36 (Figma 34), secciones 22 (20), logo 22 con símbolo de marca (24/30). |
| SCR-11 Primera vez | Coincide: tarjeta, círculo, dos botones y mosaicos. Con letra grande los botones bajan de línea. |
| SCR-12 Sin canciones | Coincide: círculo elevado y botón de acento. Sin «Abrir Ajustes» ni engrane. El bloque queda unos dp más abajo que en Figma. |
| SCR-40 Buscar | Coincide: campo hundido, grupos, resaltado lima y chevrons. «Borrar búsqueda» mide 48 (Figma 44, INV-06). Separación portada-texto 12 (14), como en Biblioteca. Título 28 (30). |
| SCR-41 Sin resultados | Coincide: campo hundido, círculo elevado y textos. |
| Accesibilidad | Encabezados con rol, objetivos ≥ 48 dp, «sin resultados» se anuncia. Con letra del sistema a 1.8 todo se desplaza; los dos mosaicos se igualan en alto. El resaltado es solo color, pero el texto se lee sin él. **No probé TalkBack.** |

Arreglos que salieron de la crítica: relieve del campo invisible por el orden de los modificadores, artista recortado en la esquina del carrusel, círculo en claro y mosaicos de distinto alto.

## Movimiento (`animate`)
- **Inicio:** cambiar de pestaña pasa decenas de veces al día, así que usa la cascada existente (fundido + 8 dp, 300 ms en total, DEC-28) en orden de lectura.
- **Buscar:** cascada solo al aparecer los primeros resultados. Al seguir escribiendo no se anima nada.
- Con «reducir movimiento» no hay cascada (comprobado en el emulador).
- **Por ver en el Redmi:** la sensación de la cascada de Inicio.

## Comprobaciones
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug`: **BUILD SUCCESSFUL**. Lint: 0 errores y 7 avisos. Dos ya existían (`VectorRaster`, `ExportedService`) y cinco son versiones nuevas de AGP, Lifecycle y Compose, que no toqué.
- **Unitarias: 95, todas pasan** (65 antes).
- **Compose e instrumentadas en el emulador: 80, todas pasan** (62 antes; 64 tras la Parte A), completas tras el último cambio de código.
- **Restauración:** `RestorePlaybackTest` destruye y recrea el servicio de verdad (ExoPlayer nuevo, comprobado en el registro) con audios propios: actividad y servicio recreados, aleatorio, canción actual borrada y canción ilegible.

### Prueba manual en el emulador (cola de «Mar adentro», Sal movida al primer lugar, minuto 0:46)
| Prueba | Resultado |
|---|---|
| Forzar cierre en pausa y reabrir | Misma canción, 46 912 ms exactos, mismo orden, origen «Mar adentro», en pausa ✅ |
| Quitar la tarea en pausa y reabrir | Mismo minuto y canción, en pausa ✅ |
| Quitar la tarea sonando | Sigue sonando, como antes (INV-11); al volver se pudo pausar ✅ |
| Forzar cierre sonando | Vuelve en pausa en 72.6 s; el último dato medido, justo antes de cerrar, era 74.1 s (≤ 5 s) ✅ |
| Borrar por fuera canciones de la cola | Se omiten sin aviso ✅ |
| Reinstalar encima (actualizar) | Conserva la reproducción ✅ |

- **El gesto de deslizar no lo pude reproducir con adb** (recientes no lo toma). Quité la tarea con `am stack remove`, y el proceso siguió vivo en los dos casos. **Falta que Brian deslice en el Redmi.**
- **Actualizar desde v1 (SC-007):** cubierto por `MigrationTest`, no con un APK viejo.
- **Escaneo:** sin música avisa «No encontramos música en tu teléfono»; con música, «Encontramos 25 canciones» y aparece Inicio. El MediaStore ya había indexado los archivos al copiarlos, así que falta ver un archivo que el sistema no conociera.
- **Notificación:** al abrir con una cola restaurada, Media3 publica su notificación en pausa, sin servicio en primer plano.
- **Pestañas:** un detalle abierto en cada una, ida y vuelta, «atrás» y tocar la activa ✅
- **2 025 canciones en `perfil`:** reproducir, pausar, restaurar y buscar funcionan. El emulador dibuja por software y salta cuadros (30 a 47 en las cargas): **la fluidez no se puede juzgar aquí.** No probé arrastrar en la cola con 2 025. Ya borré los audios de carga.
- **Redmi:** no apareció en adb; no se instaló `perfil`.
- **Emulador:** los 12 MP3 de `t06` ahora duran 93 s (uno, 8 s) y tienen carátula, para poder probar el minuto. El orden de Canciones volvió a Título A–Z porque las pruebas desinstalan la app.

## Capturas (`ai/handoff/capturas/`)
`t07-inicio.png`, `t07-inicio-primera-vez.png`, `t07-sin-canciones.png`, `t07-buscar.png`, `t07-sin-resultados.png`, `t07-opciones-album.png`, `t07-restaurada.png`.

## Ponytail
Corrí la revisión antes de cada commit de código. Hallazgos corregidos: las pruebas de restauración pasaban sin destruir el servicio, y el repositorio falso de `LibraryScreenTest` no compilaba con `scan()`.

Sin prueba propia: `addToQueue` con varias canciones y aleatorio encendido.

## Git
Rama del worktree subida a `origin development` (avance rápido). Sin autoría de IA en los mensajes.
