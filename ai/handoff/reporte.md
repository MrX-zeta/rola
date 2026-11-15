# Reporte · Tarea 03 · Reproductor completo

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Adelanté el worktree a `origin/development` con fast-forward. `git log` incluye `da415b0 docs: sesión de diseño tarea 03`.
- Usé las skills `figma-design-to-code` (antes de leer Figma), `animate` (para planear la transición y los gestos), `design-critique`, `ponytail-review` y Spec Kit (`/speckit-specify` → `-clarify` → `-plan` → `-tasks` → `-analyze` → `-implement`).
- Ponytail: off en los Pasos 1–5 y full desde el Paso 6, como pide la tarea (Brian lo cambió).
- Dispositivos: emulador Pixel 7 Pro. El Redmi no apareció en adb durante la implementación, así que **no le instalé `perfil`**.

## Qué hice
**Spec Kit** (`specs/002-reproductor-completo/`):
- Spec con 6 historias y 48 requisitos.
- Aclaraciones respondidas con el grafo y la tarea.
- Plan, investigación, modelo de datos, contratos, guía de validación y 40 tareas.
- `/speckit-analyze` sin problemas críticos; corregí 4 detalles. Commit `d75834a`.

**Servicio de reproducción** (`core/media`, commit `cc24625`):
- **Aleatorio:** es un modo que se enciende y se apaga. El orden siempre empieza en la canción actual, también al cargar una lista con el modo encendido, para que suene toda la cola.
- **Repetir:** apagado → toda la cola → una canción. Anterior usa `seekToPrevious` (reinicia la canción si lleva más de 3 s).
- **Temporizador:** 15, 30, 45 o 60 min, o «al terminar la canción». Vive en el servicio, solo **pausa** y se cancela con «Apagado». Publica su estado en los extras de la sesión.
- **Dos órdenes propias:** `rola.ORDEN_ALEATORIO` (para mover en la cola con el aleatorio encendido) y `rola.TEMPORIZADOR`. Solo las acepta el propio paquete de Rola, y el servicio valida los argumentos: el orden debe ser una permutación exacta y la opción, un nombre válido.
- **Conexión con la interfaz:** la cola llega en un `StateFlow` aparte. Quité la publicación del estado cada 500 ms: ahora se publica solo cuando hay eventos.

**Pantallas** (`feature/player`, commit `0a237c7`):
- **Reproduciendo (SCR-51):** barra de avance propia de 48 dp que se toca y se arrastra, y tiempos en Geist Mono.
- **Cola (SCR-52):** reordenar arrastrando el asa, quitar deslizando y aviso con «Deshacer». El lector de pantalla tiene las acciones «Subir», «Bajar» y «Quitar de la cola».
- **Hoja del temporizador (SCR-53):** muestra cuánto falta y el botón del temporizador queda marcado mientras está activo.
- **Color de la portada:** tiene variante clara (DEC-16) y cambia con un fundido corto entre canciones.

**Movimiento** (commit `2f46263`):
- **Transformación de contenedor:** la capa crece desde los límites de la tarjeta hasta la pantalla completa, siguiendo el dedo. Usa un recorte en `graphicsLayer`, así que el gesto no recompone.
- **Al soltar:** resorte con la velocidad del dedo. Cerrar con la flecha o Atrás dura 200 ms con EaseOut. La animación arranca en el primer cuadro dibujado.
- **Reducir movimiento:** fundido corto, sin recorte y sin seguir al dedo.
- **Gestos de la tarjeta:** el eje dominante gana (horizontal: siguiente/anterior; arriba: abrir). Umbral: 35 % del recorrido o 700 dp/s.
- **Línea de progreso continua (plan 006):** avanza en cada cuadro solo en el dibujo. Los textos de tiempo cambian una vez por segundo. Con reducir movimiento, salta de segundo en segundo.

## Pruebas y comprobaciones
- **Compilación y lint:** `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
  - Lint: 0 errores. Quedan 2 advertencias que ya existían (`VectorRaster` en `splash_icono.xml` y `ExportedService` en el manifiesto).
- **JUnit:** 12 pruebas nuevas en verde:
  - `QueueOrderTest`: mover, deshacer, aleatorio desde la actual y permutaciones.
  - `SleepTimerTest`: cada opción, «al terminar la canción», cancelar y reemplazar.
  - `SwipeTest`: distancia, velocidad, lanzamiento en contra y sin desplazamiento.
  - El formato de tiempos ya lo cubre `DurationTest`.
- **Compose:** `connectedDebugAndroidTest` → 16/16 en verde:
  - `NowPlayingTest`: título, tiempos y los estados de aleatorio y repetir.
  - `PlayerSheetTest`: con reducir movimiento no se desplaza; sin él, crece desde la tarjeta.
  - `QueueScreenTest`: muestra la canción actual y las que siguen, o el texto sin más canciones.
  - Siguen en verde `MiniPlayerTest`, `LibraryScreenTest` y `RolaNavBarTest`.
- **En el emulador:**
  - Abrir tocando y deslizando; cerrar con la flecha, deslizando y con Atrás.
  - Capturas a mitad del gesto: el contenedor sigue al dedo.
  - Gestos de la tarjeta: izquierda → siguiente, derecha → anterior, un gesto corto o lento no cambia nada.
  - Quitar, deshacer y reordenar en la cola, con el aleatorio apagado y encendido. «Siguiente» respeta el orden nuevo.
  - Aleatorio que empieza en la actual; repetir una canción con su ícono.
  - Temporizador «al terminar la canción»: pausó en 19.985 ms de 19.985 y volvió a apagado.
  - La música siguió sonando al salir de la app (INV-11).
- **Temporizador de 1 min:** no lo corrí. «Al terminar la canción» prueba la misma pausa, y la opción por tiempo está cubierta con JUnit.
- **Reducir movimiento:** no activé «Quitar animaciones» en el emulador (es un ajuste del sistema); lo cubre `PlayerSheetTest`.

### Errores encontrados y corregidos al probar
- **Deshacer volvía a quitar la canción:** el estado del gesto se guardaba por clave de fila. Ahora usa `remember` en vez del estado guardado.
- **Portadas naranjas caían al color del tema:** Palette no da tonos oscuros para ellas. Ahora se oscurece el tono vivo.
- **Contraste en claro sin portada:** el acento de respaldo era lima sobre fondo claro (INV-06). Ahora usa `primary` (lima en oscuro, oliva en claro).
- **`ponytail-review`:**
  1. Un lanzamiento rápido hacia abajo sobre la tarjeta abría Reproduciendo. Ahora solo abre si el gesto va hacia arriba.
  2. Cerrar el reproductor se llevaba el aviso de «Deshacer». Ahora los avisos viven en `RolaNavHost`.
- **Bucle de cuadros sin duración:** pedía cuadros aunque la duración fuera 0, y eso rompía `MiniPlayerTest`. Ahora solo corre si la duración es mayor que 0.

## Capturas (`ai/handoff/capturas/`)
- `t03-reproduciendo.png`, `t03-cola.png` y `t03-temporizador.png`, en oscuro.
- Las mismas con `-claro`. Para tomarlas cambié el tema solo en local; no está versionado.
- `t03-cola-deshacer.png`: el aviso con «Deshacer».
- `t03-abriendo.png`: la capa a mitad del gesto.
- **Videos:** no van. Brian indicó que bastan las capturas.

## Diferencias con Figma (design-critique)
- **SCR-51 Reproduciendo:**
  - La portada no tiene sombra: DEC-27 no pone relieve en esta pantalla (**pregunta 1**).
  - No están la pista ni el botón «Letra», el ⋮ ni «Añadir a una lista» (otras tareas). Por eso la fila de acciones queda solo con temporizador y cola, en los extremos.
  - En claro, la pista de la barra es gris claro (contenido al 12 %); en Figma se ve casi negra.
  - La etiqueta «REPRODUCIENDO DESDE» mide 12 sp (token `timeSmall`); en Figma, 11 sp.
  - El título no lleva el tracking de −0.42 de Figma, porque DEC-16 pide letterSpacing 0.
- **SCR-52 Cola:**
  - No está «Limpiar» (**pregunta 2**).
  - Las filas usan el estilo de Biblioteca (14 y 14 sp, portada con radio de 8 dp por DEC-16); en Figma son 15 y 13 sp con radio de 3.6.
  - El aviso tiene radio de 12 dp (`shapes.small`); en Figma, 14.
  - El ecualizador es estático: marca la canción, no decora (DEC-28).
- **SCR-53 Temporizador:**
  - La hoja mide lo que ocupa su contenido; en Figma tiene un alto fijo de 560.
  - La opción marcada es la vigente («Apagado»); Figma muestra «30 minutos» como ejemplo.
- **En oscuro y claro**, colores, tamaños y espaciados coinciden con los nodos 36:1465/51:4482, 36:1557/51:4570 y 36:1651/51:4662.

## Decisiones y supuestos
- Reproduciendo es una capa encima de la app, no un destino del NavHost, para que siga al dedo y nazca de la tarjeta.
- Supuestos de la spec:
  - El origen siempre dice «Biblioteca».
  - Tocar una fila de la cola no hace nada.
  - El temporizador por tiempo sigue corriendo aunque se pause a mano.
  - Lo ya escuchado no aparece en la cola.
- **Ícono de «repetir una canción»:** `repeat_one` no está en la librería de Figma. Usé el `repeat_one` estándar de Material, el mismo trazo que el `repeat` de Figma más el «1».
- **Token nuevo** `Typography.timeSmall` (Geist Mono 12/16), para los tiempos y las etiquetas de la cola.
- **API inestables de Media3:** `setShuffleOrder`, `DefaultShuffleOrder` y `pauseAtEndOfMediaItems` están marcadas `@UnstableApi`. Marqué `PlaybackService` con `@OptIn(UnstableApi::class)`.
- **Respaldo de color sin portada:** `primary`/`onPrimary`. En claro, el botón de reproducir de una canción sin portada queda oliva con ícono blanco, no lima (**pregunta 3**).
- **Sin dependencias nuevas.**
- **Emulador:**
  - Se quedó sin espacio. Con tu permiso borré los archivos de tareas anteriores en `/sdcard` (`s*.raw`, videos, `a.png`, `ui.xml`, `u.xml`).
  - También borré un apk viejo y un `t.txt` de `/data/local/tmp`.
  - Desinstalé y reinstalé Rola de depuración.

## Dudas para Brian
1. ¿La portada de Reproduciendo lleva la sombra de Figma? DEC-27 no la permite; la dejé plana.
2. ¿Implementamos «Limpiar» en la Cola? Está en Figma pero no en la tarea.
3. Sin portada en tema claro, ¿el botón de reproducir debe ser lima (DEC-16) aunque la barra y el título usen oliva por contraste?
4. La mini tarjeta en claro ya usa la variante clara del color de la portada, pero SCR-50 claro sigue «pendiente» en el grafo. ¿Se confirma?
5. Propuestas para el grafo, no las edité:
   - Agregar `repeat_one` a la librería de Figma.
   - Agregar el token `timeSmall` a DEC-16.

## Pendiente o riesgos
- **Umbrales de los gestos sin calibrar:** 35 % del recorrido o 700 dp/s. Falta probarlos en el Redmi.
- **Portada grande borrosa:** se pide en 512 px (`SongCoverFetcher`), así que en Reproduciendo (~1200 px) se ve suave. Para corregirlo, pedir el tamaño real en otra tarea.
- **Órdenes antes de conectar:** las que llegan antes de que el controlador se conecte se pierden (atajo de la tarea 02). En el emulador se nota en el primer toque después de un arranque en frío.
- **Fluidez:** no medí la transición en el Redmi con `perfil`.
