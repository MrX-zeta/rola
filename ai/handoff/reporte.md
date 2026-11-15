# Reporte · Tarea 02d · Retraso de la barra y despliegue de Biblioteca

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Adelanté el worktree a `origin/development` con fast-forward (estaba en `main` viejo y no tenía commits propios).
- `git log` incluye `a7eca18 docs: sesión de diseño tarea 02d`.
- Skills: `animate` y `ponytail-review` estaban disponibles y las usé. Esta tarea no necesitaba Figma ni Spec Kit.

## Paso 1 · Medir (antes de cambiar código)

**Dónde se midió:**
- El Redmi está conectado, pero **rechaza los toques por `adb`**: MIUI pide activar «Depuración USB (ajustes de seguridad)» y ese ajuste no lo toco yo. Brian eligió medir en el emulador (Pixel 7 Pro, API 36, 60 Hz, 1440×3120, tipo `perfil`).
- **Límites del emulador:**
  - Solo tiene 10 canciones; el Redmi tiene 375.
  - Su render es lento en **todos** los cuadros (~44 ms en la primera sesión), así que casi todos pasan de 16 ms en el total.
  - Por eso separé el **hilo principal** (Vsync → SyncQueued) del **hilo de render + GPU** (SyncStart → FrameCompleted).

**Método:**
- Por cada cambio: `gfxinfo reset`, `input tap`, 1.2 s de espera y `gfxinfo framestats`.
- 10 cambios por dirección.
- Los percentiles salen de las filas de framestats sin marcas.
- Los scripts quedaron en `app/build/t02d/`, que git ignora.

### Antes

**Hilo principal (ms)**

| Dirección | Cuadros | Lentos > 16 ms | p90 | p95 | p99 | Máx |
|---|---|---|---|---|---|---|
| Biblioteca → Buscar | 150 | 46 (31 %) | 24.1 | 27.9 | 36.8 | 50.9 |
| Biblioteca → Inicio | 154 | 39 (25 %) | 20.3 | 21.5 | 27.2 | 28.9 |
| Buscar → Biblioteca | 124 | 40 (32 %) | 47.2 | 95.7 | 194.9 | 322.7 |
| Inicio → Biblioteca | 138 | 50 (36 %) | 24.4 | 53.5 | 74.8 | 75.8 |

**Hilo de render + GPU (ms):** p90 entre 42.6 y 44.0 en las cuatro direcciones, con 94–99 % de cuadros lentos. Es el emulador, no la app.

**Total del cuadro:** 99–100 % de cuadros lentos en todas las direcciones; p99 entre 116 y 376 ms.

### Trazo (atrace con secciones de la app, 3 idas y vueltas)
Separé el trabajo propio del hilo principal (doFrame menos `postAndWait`, que es la espera al hilo de render).

**Al salir de Biblioteca:**
- Unos 210–240 ms después del toque, cuando la píldora todavía se mueve, cae un cuadro de **17–32 ms de trabajo propio**.
- Es Biblioteca **desmontándose** al terminar el fundido: `Compose:applyChanges` 10 ms, `Compose:onForgotten` 7–8 ms y `onPositionedCallbacks` 4 ms.
- Esto explica por qué Brian lo siente solo en esa dirección: el tirón llega en plena animación.

**Al entrar a Biblioteca:**
- El primer cuadro rearma toda la lista: **130–140 ms** de trabajo propio en el emulador (`measureAndLayout` 85–106 ms).
- Cae al principio, antes de que la píldora avance, y por eso se nota menos.

**La etiqueta de la píldora no aparece como causa.** El plan B (barra sin texto) **no se justificó** y no lo apliqué.

## Paso 2 · Corrección: Biblioteca ya no se desmonta (`d37782b`)

**Qué cambió:**
- `LibraryScreen` se monta una sola vez junto al `NavHost`, no dentro de él.
- Cuando no es la pestaña activa no se coloca: no se dibuja, no recibe toques y no está en el árbol de accesibilidad. Lo comprobé con `uiautomator dump`: en Buscar solo aparecen «Próximamente» y «Buscar».
- Su destino en el `NavHost` queda vacío y sigue llevando la pila, así que atrás del sistema funciona como antes (DEC-03).
- El fundido es el mismo de las otras pestañas (150 ms de entrada con `EaseOut`, 100 ms de salida).
- El ViewModel ahora vive en la Activity. Antes vivía en la entrada de Biblioteca, que nunca salía de la pila porque es el destino de arranque.

**Antes / después** (mismo emulador y misma sesión):

| Hilo principal | Antes p99 / máx | Después p99 / máx |
|---|---|---|
| Biblioteca → Buscar | 36.8 / 50.9 | 35.2 / 35.6 |
| Biblioteca → Inicio | 27.2 / 28.9 | 24.2 / 30.7 |
| Buscar → Biblioteca | 194.9 / 322.7 | 29.4 / 41.6 |
| Inicio → Biblioteca | 74.8 / 75.8 | 26.9 / 32.5 |

**Trabajo propio del hilo principal en el trazo** (700 ms tras cada toque):

| | Antes | Después |
|---|---|---|
| Biblioteca → Buscar: peor cuadro | 31.7 ms | 17.0 ms |
| Biblioteca → Buscar: `onForgotten` por toque | 7.8–8.2 ms | 0.6–0.9 ms |
| Buscar → Biblioteca: peor cuadro | 139.9 ms | 17.2 ms |

**Lo que queda:**
- Los primeros 1–3 cuadros de **cualquier** cambio de pestaña (~10 ms de `recompose` del cambio de destino), iguales en las dos direcciones. Ya no hay asimetría.
- El conteo «lentos > 16 ms» del hilo principal casi no cambia en el emulador, porque ese hilo también espera al render lento.

**Commit:** `perf(navegacion): Biblioteca no se desmonta al cambiar de pestaña`. Incluye la corrección del comentario viejo de `RolaNavHost.kt`.

## Paso 3 · Despliegue de Biblioteca (`86c14ea`, skill `animate`)

**Decisiones de la skill `animate`:**
- Se ve decenas de veces al día, así que debe ser breve y sutil.
- Propósito: que la lista no aparezca de golpe.

**Cómo es:**
- Las primeras 8 filas visibles hacen fundido y suben 8 dp.
- Tiempos: 25 ms entre filas y 125 ms por fila con `RolaMotion.EaseOut`. Total: 7 × 25 + 125 = **300 ms**.
- Solo usa `alpha` y `translationY` en `graphicsLayer`. El reloj se lee al dibujar, así que no recompone.
- No bloquea toques.

**Cuándo pasa:**
- Cada vez que Biblioteca se vuelve visible. Empieza en el mismo cambio de ruta que el fundido de la pestaña, y el primer cuadro ya dibuja las filas en su punto de inicio.
- Si hay esqueletos, ocurre cuando llegan los datos, una sola vez.
- No se repite al rotar: la marca se guarda con `rememberSaveable`.
- Las filas que llegan al desplazar y las que pasan de 8 aparecen normales.
- Al salir espera los 100 ms del fundido antes de reiniciar, para no cortar la salida.
- Con reducir movimiento no hay cascada.

**Pruebas de Compose nuevas** (en `LibraryScreenTest`):
- Con reducir movimiento, la posición de cada fila en el primer cuadro es igual a su posición final.
- Control: sin reducir movimiento, en el primer cuadro sí están desplazadas.

**Costo (A/B en las mismas condiciones):**
- El emulador se puso más lento después de las pruebas (render ~95 ms en todas las direcciones), así que comparé contra un build temporal con la cascada apagada, sin versionar.

| Hilo principal p99 / máx | Con cascada | Sin cascada |
|---|---|---|
| Buscar → Biblioteca | 25.5 / 28.7 | 37.3 / 42.0 |
| Inicio → Biblioteca | 26.9 / 28.9 | 42.8 / 53.1 |

La cascada no agrega costo medible; la diferencia es ruido del emulador.

## Paso 4 · Comprobaciones
- `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
- **Unitarias:** 22, 0 fallas.
- **Compose** (`connectedDebugAndroidTest`, Pixel 7 Pro API 36): **11, 0 fallas** (9 de antes + 2 nuevas).
- **Lint:** 0 errores y los mismos 2 avisos de siempre: `VectorRaster` (splash) y `ExportedService` (Media3).
- **Videos** (emulador, `perfil`, 720×1280):
  - `ai/handoff/capturas/t02d-barra.mp4`: ocho cambios de pestaña y, al final, dos toques a 120 ms.
  - `ai/handoff/capturas/t02d-despliegue.mp4`: entradas a Biblioteca desde Buscar e Inicio.
  - No pude sacar cuadros sueltos para revisarlos (no hay `ffmpeg`), así que conviene verlos.
- **Redmi:** le instalé el `perfil` final encima del anterior, con la misma firma.
- **Nota del entorno:** las pruebas de Compose reinstalan la app y quitan el permiso de audio. En el emulador lo volví a dar con `pm grant` para seguir midiendo.

## Dudas y pendientes para el equipo
1. **Ver en el Redmi** si ya no se siente el retraso al salir de Biblioteca y si la cascada se siente bien. Las medidas son del emulador, que no reproduce bien al Redmi (120 Hz, 375 canciones).
2. **Medir en el Redmi:** si Brian activa «Depuración USB (ajustes de seguridad)», puedo repetir la medida ahí con los mismos scripts.
3. **A futuro:** cuando Biblioteca tenga su propia pila (álbum, artista…), esas pantallas irán en el `NavHost` y la raíz seguirá fuera. Si eso no convence, la alternativa es volver a montarla dentro y aceptar el costo de rearmarla. Propongo anotarlo en el grafo junto a DEC-03.

## Commits
- `d37782b` · perf(navegacion): Biblioteca no se desmonta al cambiar de pestaña
- `86c14ea` · feat(motion): despliegue de Biblioteca
- (este) docs(handoff): reporte de la tarea 02d
