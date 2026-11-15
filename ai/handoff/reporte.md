# Reporte · Tarea 04 · Cola sin fugas (librería) y onboarding completo

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Adelanté el worktree a `origin/development` con fast-forward. `git log` incluye `766499b docs: sesión de diseño tarea 04`.
- Skills usadas: `speckit-converge`, `speckit-specify`, `-clarify`, `-plan`, `-tasks`, `-analyze` e `-implement`; `figma-design-to-code` (antes de leer Figma), `animate` (movimiento del onboarding), `design-critique` y `ponytail-review` (antes de cada commit). No faltó ninguna.
- Ponytail: full en la Parte A, off de B1 a B5 y full desde B6, cambiado por Brian en cada punto.
- Dispositivos: emulador Pixel 7 Pro. El Redmi apareció en adb al empezar, pero al final ya no estaba. **No le instalé `perfil`**; el APK está en `app/build/outputs/apk/perfil/app-perfil.apk`.

## Parte A · Cola sin fugas
- **Librería:** `sh.calvin.reorderable:reorderable` **3.1.0** (la estable más nueva en Maven Central; Apache 2.0; sin red). Quité todo el arrastre propio: vista previa, `reordenar`, `limitar` y el autodesplazamiento con `withFrameNanos` (unas 90 líneas menos). Solo las filas de «A continuación» son `ReorderableItem`; «Sonando ahora», la fila actual y las etiquetas no son destinos (FR-059).
- **Cola que cambia a mitad del arrastre (DEC-24, FR-058):** el orden del arrastre se guarda como lista de ids y se aplica sobre la cola viva (`ordenComo`). Si termina la canción, el arrastre sigue y al soltar el movimiento se aplica sobre la cola nueva; si la fila arrastrada pasó a sonar, no se mueve nada. Quité el `shortcut:` de la vista previa: ahora `moveUpNext` devuelve si mandó la orden y, si no la mandó, la vista suelta su orden al instante (ya no puede quedar un orden falso).
- Se conservan: tocar para reproducir, quitar deslizando con «Deshacer», «Limpiar» y las acciones «Subir», «Bajar» y «Quitar de la cola».
- **Spec 002:** FR-051 actualizado (marquesina mientras suena, 2 s entre pasadas; ya lo cumplía el código), FR-058 y FR-059 nuevos y la dependencia en `plan.md`. `/speckit-converge` agregó la Fase 12 (T057–T060), todas hechas.
- **Emulador, app real** (45 audios de prueba de 2:30, ya borrados): cola de 44 con la lista a la mitad.
  - Arriba: «Pista 20» sube con el autodesplazamiento hasta arriba, se sostiene 7 s y al soltar queda **primera** de «A continuación».
  - Abajo: «Pista 15» baja hasta el final, se sostiene 8 s y queda **última**.
  - A mitad del arrastre mandé «siguiente» con la tecla multimedia: el arrastre siguió y «Pista 08» quedó primera en la cola nueva.
- **Capturas:** `t04-cola-arriba-sosteniendo.png`, `t04-cola-arriba-soltada.png`, `t04-cola-abajo-sosteniendo.png`, `t04-cola-abajo-soltada.png`, `t04-cola-cambia-sosteniendo.png` y `t04-cola-cambia-soltada.png`. **No grabé `t04-cola-arriba.mp4` ni `t04-cola-abajo.mp4`**: Brian pidió antes solo capturas.
- Commits: `docs(spec): 002 cola con librería` y `fix(player): cola con reorderable`.

## Parte B · Onboarding
- **B1–B5:** `specs/003-onboarding/` con spec (22 FR), aclaraciones de la tarea, plan, research, data-model, contratos, quickstart y 25 tareas. `/speckit-analyze` no encontró nada crítico. Commit `docs(spec): 003 onboarding`.
- **Implementado:**
  - `core/data/PreferencesRepository.kt`: interfaz y DataStore (`androidx.datastore:datastore-preferences` 1.3.0, la dependencia permitida), con la clave `onboarding_completed` en el archivo `rola_ajustes`. Si no se puede leer se toma «no completada»; si no se puede escribir (disco lleno), la app no se cierra.
  - `MainActivity`: el splash del sistema sigue hasta leer el valor, así que no parpadea otra pantalla. Orden: bienvenida → Permisos → Biblioteca.
  - `feature/onboarding/OnboardingScreen.kt`: `HorizontalPager` de 3 páginas, «Omitir» en las páginas 1 y 2, botón «Siguiente»/«Empezar» y Atrás que vuelve una página.
  - `OnboardingIllustrations.kt`: portadas en abanico, tarjeta de letra y wifi tachado con el interruptor decorativo, todo con formas del tema y el relieve de `Relief.kt` (`elevado`, `acento`).
  - Permisos: queda solo «Música y audio» (quité la fila de Notificaciones, sus textos y `ic_notifications.xml`). Permiso denegado no necesitó cambios.
- **Movimiento** (skill `animate`; primera vez → cabe el deleite, todo con `RolaMotion`):
  - Indicador: la marca activa se estira hacia la página nueva y luego se recoge. Sigue al dedo porque lee la posición del pager al dibujar, sin animación propia.
  - Ilustraciones: al alejarse del centro bajan a escala 0.94 y opacidad 0.6. La primera entra al primer cuadro dibujado (0.96→1 y fundido, 250 ms con `EaseOut`).
  - Botón: tamaño fijo; el texto cambia con un fundido (entra en 150 ms, sale en 100 ms). «Omitir» se funde al llegar a la página 3.
  - Reducir movimiento: las páginas no se desplazan, solo se funden; sin escala ni entrada.
- **Pruebas:**
  - JUnit `PreferencesRepositoryTest` (3), con DataStore real en un archivo temporal.
  - Compose `OnboardingTest` (5), con un repositorio falso en memoria: «Siguiente» hasta «Empezar»; «Omitir» guarda y muestra Permisos sin «Notificaciones»; con reducir movimiento la página nueva no se desplaza; con la bienvenida completada y el permiso abre directo; mientras se lee no se muestra nada.
- **Emulador:** recorrido completo desde `pm clear`. Al reabrir con el permiso, entra directo a Biblioteca. Rotar en la página 2 conserva la página. Con letra al 200 % los textos caben y las ilustraciones se encogen para caber.
- **Capturas:** `t04-onboarding-1.png`, `t04-onboarding-2.png`, `t04-onboarding-3.png` y `t04-permisos.png`.
- Commits: `feat(data): preferencias con DataStore` y `feat(onboarding): bienvenida de tres páginas y permisos según Figma`.

### `design-critique` (SCR-02 a SCR-06 contra Figma, oscuro)
Coinciden en estructura, colores, relieve y textos. Diferencias menores:
- SCR-03: la línea activa usa `headlineSmall` (24 sp) en vez de 26 sp, así que cabe en una línea (en Figma usa dos). Título 16 sp en vez de 15; artista y tiempo 12 sp en vez de 13. Son los estilos del tema más cercanos (INV-04).
- SCR-04: el interruptor es el `Switch` de M3 apagado, muy parecido al de Figma.
- SCR-05: título en `headlineLarge` (32 sp) en vez de 34, como ya estaba.
- SCR-06: la ruta de Ajustes tiene radio de 20 dp (`shapes.medium`) y en Figma es de 16; no hay token de 16. La pantalla no cambió en esta tarea.
- Tema claro: la app sigue en oscuro fijo (DEC-15), así que el claro solo se revisó con las vistas previas `@Preview`; no hay capturas claras.

## Comprobaciones finales
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**. Unitarias: **44 OK**. Lint: 0 errores y las 2 advertencias de antes (`VectorRaster` en `splash_icono` y `ExportedService`).
- `:app:connectedDebugAndroidTest` → **33 OK**, sin fallos.
- `ponytail-review`:
  - Parte A: «Ship»; una sugerencia de -3 líneas que no apliqué.
  - Parte B: un hallazgo (un disco lleno cerraba la app al tocar «Empezar»), ya arreglado; una sugerencia de -8 líneas que no apliqué (unir los dos ayudantes que encogen las ilustraciones).
- `git log` sin autoría de IA.

## Dudas y decisiones
- **Pruebas de Compose con autodesplazamiento largo de la cola:** con el reloj manual de Compose, la librería avanza ~1 fila/s y una prueba se colgó. Quité esas tres pruebas y lo dejé como `shortcut:` en `QueueScreenTest`; lo verifiqué en la app real con capturas. Si se quiere automatizar, propongo Maestro.
- **La fila arrastrada ya no se limita a la lista** (FR-057): la librería la deja bajo el dedo aunque pase por el encabezado; `LazyColumn` la recorta en su borde, así que nunca se ve fuera de la lista. En el orden sí se cumple.
- **KV-DATASTORE sigue «propuesto»** en el grafo; lo usé porque DEC-18 (decidido) y la tarea piden `onboarding_completed`. Solo creé esa clave. Propongo pasarlo a «decidido».
- **Instalaciones de tareas anteriores** que ya tenían el permiso pero no la bienvenida la ven una vez (DEC-18 solo mira `onboarding_completed`). ¿Se prefiere saltarla en ese caso?
- **«Omitir» no está en la página 3**, como en Figma 22:383 (ahí está «Empezar»). Ajusté FR-006.
- **Colores de las portadas de ejemplo** (`#2F4BB8`, `#E4572E`, `#1F7A5C` y sus tonos claros) quedaron como tokens en `Color.kt` (`RolaIlustracion`), no en las pantallas.
- **Emulador lleno:** quedó al 94 %, con archivos viejos en `/sdcard` (`s1.raw`…`s10.raw`, ~180 MB, y videos `rec_nav*.mp4`) que no son de esta tarea y no borré. Varias instalaciones fallaron por espacio hasta quitar mis audios de prueba. ¿Los borro?
- **Redmi:** queda pendiente instalarle `perfil` para juzgar la fluidez del indicador y de la cola (DEC-28 `fluidez`).
