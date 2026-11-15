# Reporte · Tarea 03c · Arrastre de la cola sin fugas

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Adelanté el worktree a `origin/development` con fast-forward. `git log` incluye `337c4b3 docs: sesión de diseño tarea 03c`.
- Skills usadas: `speckit-converge` y `ponytail-review`. No hubo pantalla ni animación nueva, así que no usé `figma-design-to-code`, `animate` ni `design-critique`.
- Ponytail: full en todos los pasos.
- Dispositivos: emulador Pixel 7 Pro (API 36). El Redmi apareció un momento en adb a mitad de las pruebas y luego desapareció. Al final no estaba, así que **no le instalé `perfil`**.

## Paso 1 · Spec
- Agregué **FR-056** (la fila se queda bajo el dedo) y **FR-057** (el autodesplazamiento se detiene en el inicio y el final, y la fila nunca sale de los límites) a `spec.md`, citando DEC-24.
- `/speckit-converge` agregó la **Fase 11: Convergencia** con T054–T056. Las tres quedaron hechas y marcadas.
- Commit `docs(spec): 002 arrastre de la cola`.

## Paso 2 · Causas confirmadas y arreglo (`QueueScreen.kt`)
- **Arriba:**
  - La corrección de la 03b, que conservaba la posición al mover la primera fila visible, iba en una corrutina (`scrollToItem`). Eso dejaba un cuadro de salto y además cancelaba el `scrollBy` del autodesplazamiento, porque los dos usan el mismo candado de desplazamiento.
  - Ahora uso `requestScrollToItem`, que conserva la posición en el mismo cuadro y no cancela nada.
  - La compensación del dedo ya estaba: `offsetY` suma lo que se desplaza la lista.
- **Abajo y arriba, «sale de los límites»:**
  - Confirmado: el desplazamiento de la fila crecía sin límite con el dedo, aunque la lista ya no pudiera avanzar.
  - Nueva función `limitar`: mantiene la fila dentro de la vista, sin pasar arriba del primer lugar ni debajo del último. Se aplica antes de cada intercambio.
  - El autodesplazamiento se detiene si `canScrollForward` o `canScrollBackward` es falso.
- **No usé la librería `reorderable`.** El arrastre propio quedó sólido con unas 15 líneas, sin dependencia nueva. Las acciones de accesibilidad «Subir», «Bajar» y «Quitar de la cola» no cambiaron.
- **Marquesina (DEC-04):** corre solo mientras suena, con 2 s entre pasadas (`repeatDelayMillis = 2_000`). En pausa o con reducir movimiento, queda estática con puntos suspensivos.
- Commits: `fix(player): arrastre de la cola sin fugas` y `fix(player): marquesina mientras suena`.

## Paso 3 · Pruebas y comprobaciones
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**. Lint: 0 errores y las 2 advertencias de antes (`VectorRaster` y `ExportedService`).
- **Compose:** todas las pruebas instrumentadas, **27 OK**. Las nuevas son:
  - `QueueScreenTest.arrastradaAlPrimerLugarQuedaEnIndiceCeroDentroDeLaLista`: cola de 40, la fila 3 sube 900 dp; mientras se sostiene, sigue dentro de la lista, y al soltar queda `onMove(3, 0)`.
  - `QueueScreenTest.arrastradaMasAllaDelFinalQuedaEnElUltimoIndice`: la fila 0 baja y se sostiene 10 s; la lista llega al final, la fila no sale de la vista y queda `onMove(0, 39)`.
  - `MiniPlayerTest.enPausaElTituloQuedaEstaticoConPuntos`.
  - Nota técnica: estas pruebas avanzan el reloj a mano. Con el dedo abajo, el autodesplazamiento pide cuadros sin parar y la prueba nunca llegaba a «reposo». Por eso la primera corrida se quedó colgada.
- **Emulador, app real:** armé una cola de 46 canciones (36 copias de los audios de prueba en `Music/t03c`, ya borradas) y probé con la canción en pausa.
  - Abajo: la fila 0 se sostiene al fondo 8 s; la lista se detiene al final con la fila bajo el dedo y, al soltar, queda en el índice 32 de 33.
  - Arriba: la última fila se sube hasta el encabezado y se sostiene 10 s; llega al índice 0 sin tapar «Sonando ahora» ni perderse.
- **Capturas** (`ai/handoff/capturas/`): `t03c-cola-abajo-sosteniendo.png`, `t03c-cola-abajo-soltada.png`, `t03c-cola-arriba-sosteniendo.png` y `t03c-cola-arriba-soltada.png`.
- **No grabé el video `t03c-cola-arrastre.mp4`.** Brian pidió antes solo capturas, sin videos. Si lo quieres esta vez, dímelo.

## Dudas y observaciones
- **Si la canción termina a mitad del arrastre**, la cola cambia y el arrastre se cancela; la fila vuelve a su lugar. Lo vi en el emulador porque los audios de prueba duran 19 s. Pasa desde antes y ya está marcado como `shortcut:` en el código. ¿Lo quieren resolver? Una opción es mantener la vista previa y aplicar el movimiento sobre la cola nueva.
- FR-051 de la spec aún dice «marquesina solo si no cabe». No agregué «mientras suena, 2 s entre pasadas», porque el Paso 1 pedía solo dos requisitos. Propongo agregarlo en la próxima pasada de la spec.
- `ponytail-review`: «Ship», sin hallazgos.
