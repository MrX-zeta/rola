# Reporte · Tarea 03b · Correcciones del reproductor

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Adelanté el worktree a `origin/development` con fast-forward. `git log` incluye `cea5619 docs: sesión de diseño tarea 03b`.
- Skills usadas:
  - `speckit-converge`.
  - `figma-design-to-code`, antes de leer los nodos 5:51, 36:1465 y 36:1557.
  - `animate`, para el gesto de la portada, la marquesina y el cierre.
  - `design-critique` y `ponytail-review`.
- Ponytail: off en el Paso 1 y full desde el Paso 2. Brian lo cambió las dos veces.
- Dispositivos: emulador Pixel 7 Pro (API 36). El Redmi no apareció en adb, así que **no le instalé `perfil`**.

## Paso 1 · Spec 002 y converge
- Commit `docs(spec): 002 correcciones de la revisión`.
- `spec.md` tiene una sección «Revisión · tarea 03b» con las respuestas de Brian. También:
  - Corregí FR-003, FR-007, FR-018, FR-020 y FR-039.
  - Agregué FR-049 a FR-055, SC-011 y SC-012, y escenarios nuevos en las historias 1, 3 y 4.
  - Quité los supuestos de «tocar una fila no hace nada» y «Limpiar queda fuera».
- `/speckit-converge` agregó la **Fase 10: Convergencia** con 13 tareas (T041–T053). Las 13 quedaron hechas y marcadas.

## Paso 2 · Lo que encontró Brian
1. **Gesto de la mini tarjeta** (`Swipe.kt`):
   - El umbral ahora es el menor entre el 25 % del ancho y 96 dp, o 400 dp/s. Los valores quedaron junto al gesto.
   - La distancia se mide desde donde empieza el dedo. Los toques van en una caja quieta y lo que se mueve es el contenido.
   - **Causa encontrada al probar:** el tramo final del gesto puede llegar recién en el evento de soltar. En el emulador, `input swipe` daba −181 px cuando el dedo había recorrido −420. Ahora también cuenta el evento de soltar y el rastreador de velocidad usa los eventos completos, con sus posiciones históricas. Es probable que en el Redmi esto, junto con el umbral del 35 %, explique el «solo rebota».
2. **Reproduciendo a medias** (`PlayerSheet.kt`):
   - Cada movimiento lanzaba un salto que podía llegar después de soltar y cortar la animación final. Ahora el salto se registra en el acto (`CoroutineStart.UNDISPATCHED`).
   - Si se toma la capa a medio camino, el arrastre sigue desde ahí sin saltar.
   - Si el gesto se cancela, termina cerrada o abierta.
   - Pruebas nuevas en `PlayerSheetTest`: 5 sueltas a distintas distancias y ritmos, y un gesto cancelado. Las dos terminan en 0 o 1.
3. **Reproduciendo repartida:**
   - La portada toma el alto que sobra (cuadrada, lo más grande que quepa) y los controles quedan abajo.
   - Reproducir mide 80 dp y anterior/siguiente 56 dp, como en Figma.
   - La fila de acciones queda a 16 dp de la barra del sistema.
4. **Gesto sobre la portada:** con el mismo umbral, siguiente/anterior. La portada sigue al dedo y sale por su lado mientras entra la nueva con un fundido, o vuelve con un resorte. Con reducir movimiento no se mueve.
5. **Mini tarjeta:** quedó solo con reproducir/pausa. El título usa `basicMarquee`, que solo corre si no cabe. Con reducir movimiento, el título queda estático con puntos suspensivos.
6. **Cola:**
   - Tocar una fila la reproduce. Tocar el asa no la reproduce.
   - Cerca de los bordes, la lista se desplaza sola en las dos direcciones, más rápido cuanto más cerca, hasta 800 dp/s.
   - Más allá de la primera o la última fila, la fila queda en la posición válida más cercana.
   - Al mover la primera fila visible se conserva la posición de la lista, para que la fila no se pierda.

## Paso 3 · Respuestas aplicadas
- La portada es **plana**, como antes.
- **«Limpiar»:**
  - Quita todo lo que sigue y deja la actual.
  - Muestra el aviso «Limpiaste la cola» con «Deshacer», que lo devuelve en su orden.
  - Con aleatorio, también restaura el orden aleatorio.
  - Sin canciones siguientes queda deshabilitado.
- **Sin portada en claro:** el botón de reproducir es **lima** con `onAccent`, y la barra y el título quedan en `primary`. Agregué `play` y `onPlay` a `CoverColors`; con portada son el acento de la portada.
- **Portada nítida:** `SongCoverFetcher` pide la miniatura al tamaño en que se dibuja. Palette sigue a 512 px y reutiliza la portada grande si ya está en caché.
  - En el emulador se sigue viendo pixelada porque los audios de prueba traen una portada de **240×240 px**; lo comprobé leyendo el PNG incrustado.
- **Órdenes antes de conectar:** se guardan en una fila y salen en orden al conectar. Quité el `shortcut:` de `PlayerConnection`.

## Pruebas y comprobaciones
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
  - Lint: 0 errores. Siguen las 2 advertencias de antes (`VectorRaster` y `ExportedService`).
- **JUnit**, nuevas o cambiadas:
  - `SwipeTest`: el menor entre 25 % y 96 dp, desde el centro, por velocidad, lanzamiento en contra y sin desplazamiento.
  - `QueueOrderTest`: tramos contiguos de «Limpiar».
- **Compose** (`connectedDebugAndroidTest`): **24/24 en verde** (corrida final, después de `ponytail-review`).
  - `MiniPlayerTest`:
    - Deslizar 110 dp desde el centro pasa a la siguiente; 40 dp lentos no hacen nada.
    - No hay botón siguiente.
    - Con reducir movimiento, el título queda recortado y estático; sin reducir movimiento, se mide completo para la marquesina.
  - `PlayerSheetTest`: nunca queda a medias, ni al soltar ni al cancelar.
  - `QueueScreenTest`:
    - Tocar «Despacio» llama a reproducir la posición 1.
    - «Limpiar» deja «A continuación · 0» y la actual; «Deshacer» vuelve a «· 2».
    - Sin canciones siguientes, «Limpiar» queda deshabilitado.
- **En el emulador:**
  - Desde el centro de la tarjeta: izquierda → siguiente; derecha → anterior; un arrastre corto no cambia nada.
  - En Reproduciendo:
    - Hacia arriba abre.
    - Soltar a 143 dp cierra del todo; soltar a 57 dp vuelve a abierta.
    - La portada deslizada cambia de canción.
  - Cola:
    - Tocar una fila cambia la canción.
    - «Limpiar» dejó «A continuación · 0» y «Deshacer» devolvió las 6.
- **No comprobado en el emulador:**
  - **El primer toque tras un arranque en frío.** En este emulador el controlador conecta antes de que la biblioteca termine de cargar, así que no pude provocarlo; la lógica es corta y está revisada.
  - **El autodesplazamiento de la cola.** Hay solo 6 canciones siguientes y caben en pantalla.
  - Las dos cosas hay que verlas en el Redmi.
- El emulador se cayó a mitad de una corrida de pruebas («System has crashed») y quedó muy lento. Lo reinicié con `adb reboot`. También volví a dar el permiso de audio con `pm grant`, porque las pruebas reinstalan la app.

## Capturas (`ai/handoff/capturas/`)
- `t03b-reproduciendo.png` (oscuro), `t03b-reproduciendo-claro.png` y `t03b-reproduciendo-claro-sin-portada.png` (botón lima y barra oliva).
  - Para el tema claro cambié el tema solo en local; no está versionado.
- `t03b-mini.png`: solo reproducir/pausa.
- `t03b-cola.png`: con «Limpiar».
- Sin videos.

## Diferencias con Figma (design-critique)
- **SCR-51:**
  - La portada se centra en el alto que sobra, así que en el Pixel 7 Pro queda más aire entre portada y título que en Figma (24 dp). Es el reparto que pidió la tarea; en pantallas más bajas la portada se achica.
  - Siguen fuera la pista y el botón «Letra», el ⋮ y «Añadir a una lista» (otras tareas).
- **Mini tarjeta (5:51):** coincide en portada de 48 dp, título SemiBold 14, artista 12 y botón de 44 dp con el acento.
  - La marquesina no se ve en las capturas porque los títulos de prueba son cortos; la cubre `MiniPlayerTest`.
- **SCR-52:** «Limpiar» va en Geist SemiBold 14 con `primary` a la derecha del título «Cola», como en Figma. El resto, igual que en la tarea 03 (tamaños de fila de Biblioteca y radio de 8 dp).

## Decisiones
- **Umbral único para todos los gestos:** mini tarjeta horizontal y vertical, portada y cierre. En el cierre, 96 dp de arrastre bastan, o 400 dp/s.
- **`basicMarquee` con sus valores por defecto:** 3 pasadas y después se detiene, para que no se mueva sin fin (DEC-28: no decora). Si Brian la quiere continua, es un parámetro.
- **«Limpiar» con aleatorio:** quita por tramos contiguos (una llamada por tramo) para no hacer miles de llamadas con colas grandes.
- **El asa de la cola consume su propio toque:** `ponytail-review` detectó que tocarla reproducía la canción.
- **Sin dependencias nuevas.**

## Dudas para Brian
1. ¿La marquesina se detiene tras 3 pasadas (como ahora) o corre sin fin?
2. **Cierre de Reproduciendo:** ¿96 dp de arrastre está bien? Antes era el 35 % del recorrido.
3. Probar en el Redmi la calibración (96 dp / 400 dp/s), el primer toque tras un arranque en frío y el autodesplazamiento de una cola larga.

## Commits
- `docs(spec): 002 correcciones de la revisión`
- `feat(media): tocar una fila, limpiar la cola y órdenes antes de conectar`
- `fix(player): gestos desde cualquier punto, cierre que no queda a medias y mini tarjeta con marquesina`
- `feat(player): Reproduciendo repartida, portada deslizable y nítida`
- `feat(player): cola con tocar, Limpiar y desplazamiento al arrastrar`
- `docs(handoff): reporte de la tarea 03b`
