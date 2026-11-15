# Tarea 02d · Medir el retraso de la barra y despliegue de Biblioteca

**Estado:** esperando revisión
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.4.2 · Sin Spec Kit.

## Lo que vio Brian en el Redmi (tipo `perfil`)
- La 02c mejoró, pero la píldora todavía se retrasa un poco **al salir de Biblioteca** (Biblioteca → Buscar o Biblioteca → Inicio). **Al entrar** a Biblioteca (Buscar → Biblioteca) se siente fluida.
- Que sea solo en una dirección apunta a trabajo en el hilo principal al salir de Biblioteca (desmontar la lista, cancelar portadas, Palette, el fundido con dos pantallas a la vez…), no al texto. Hay que **medir antes de cambiar**.
- La lista de canciones aparece una vez y queda estática para siempre; Brian quiere un despliegue al entrar.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 02d`. Si no, detente.

## Paso 1 · Medir (tipo `perfil`, en el Redmi si está conectado; si no, en el emulador)
- Para cada dirección (Biblioteca→Buscar, Buscar→Biblioteca, Biblioteca→Inicio, Inicio→Biblioteca), 10 cambios: `adb shell dumpsys gfxinfo com.luis.rola reset`, los toques con `adb shell input tap`, y luego `dumpsys gfxinfo com.luis.rola framestats`. Anota cuadros totales, cuadros lentos (> 16 ms) y percentiles 90/95/99 por dirección.
- Si hay cuadros lentos, toma un trazo con Perfetto (o `Debug.startMethodTracing` en un build temporal no versionado) y encuentra qué ocupa el hilo principal en esos cuadros.
- Escribe la tabla en el reporte **antes** de cambiar código.

## Paso 2 · Corregir la causa medida
- Arregla lo que el trazo señale. Ejemplos de lo que podría ser: que Biblioteca se desmonte y vuelva a armar (conservar su estado con `saveState/restoreState` o mantener la lista viva), cancelaciones de Coil o Palette en el hilo principal, el fundido de 150/100 ms con las dos pantallas dibujando, o recomposiciones de toda la pantalla por leer estado de la barra.
- **Plan B autorizado (DEC-28 `barra_sin_texto`):** solo si la medida demuestra que la etiqueta de la píldora es la causa, cambia la barra a solo íconos: tres pestañas del mismo ancho, el fondo lima se desliza, el nombre queda solo para accesibilidad. Explica en el reporte con números por qué.
- Repite la medida del Paso 1 y pon el antes/después en el reporte.
- Commit: `perf(navegacion): …` o `fix(motion): …` según la causa.

## Paso 3 · Despliegue de Biblioteca (`animate`, DEC-28 `despliegue_listas`)
- Cada vez que se entra a la pestaña Biblioteca (no al desplazar, no al volver de rotar): las filas visibles aparecen en cascada con fundido + subida de 8 dp, máximo 8 filas, 25–30 ms entre filas, total ≤ 300 ms. Las filas que llegan al desplazar aparecen normales.
- No debe retrasar el primer cuadro ni competir con la píldora: empieza junto con el fundido de la pestaña.
- Si los esqueletos se están mostrando, la cascada ocurre cuando llegan los datos (sin doble animación).
- Con reducir movimiento: sin cascada.
- Prueba de Compose: con reducir movimiento todas las filas visibles están en su estado final en el primer cuadro.
- Commit: `feat(motion): despliegue de Biblioteca`.

## Paso 4 · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- Graba `t02d-barra.mp4` y `t02d-despliegue.mp4` con `perfil`. Si el Redmi está conectado, instálale `perfil`.
- Corrige de paso el comentario viejo de `RolaNavHost.kt` («sin animaciones entre pestañas»).
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte con las tablas de medidas y estado «esperando revisión».
