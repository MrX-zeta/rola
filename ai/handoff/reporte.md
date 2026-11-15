# Reporte · Tarea 02e · Entrada a Biblioteca sin saltos

**Estado:** esperando revisión.

## Paso 0 · Comprobación
- Adelanté el worktree a `origin/development` con fast-forward. `git log` incluye `8d90c9e docs: sesión de diseño tarea 02e`.
- Skills: usé `ponytail-review`. No cargué `animate`, porque el Paso 3 ajusta una animación que ya existía y no crea una nueva. Esta tarea no necesitaba Figma ni Spec Kit.

## Paso 1 · Medir

**Dónde se midió:**
- El Redmi **sigue rechazando `adb shell input tap`** y `pm grant` (`SecurityException: … INJECT_EVENTS`), incluso después de que Brian activara el ajuste.
- Brian eligió medir en el emulador: Pixel 7 Pro, API 36, 60 Hz, tipo `perfil`.
- Para acercarlo al Redmi, copié las 10 canciones de prueba hasta tener **380** (en `/sdcard/Music/t02e/`). Las borré al terminar porque el emulador se quedó sin espacio para las pruebas.
- **Límite del emulador:** el hilo de render tarda ~90–100 ms en **todos** los cuadros (unos 10 cuadros por segundo). Así, cualquier animación de 150–300 ms dura 2–3 cuadros en cualquier dirección. Por eso no reproduce lo que Brian ve en el Redmi.

**Método:**
- Los mismos scripts de la 02d, ahora en `app/build/t02e/` (git lo ignora).
- `gfxinfo`: 10 cambios por dirección, incluido Inicio ↔ Buscar como control.
- `atrace` con secciones de la app: 6 cambios antes y 6 después, más 20 después para confirmar.
- Script nuevo `render.py`: tiempo de cada `DrawFrame` del hilo de render y cuántas capas fuera de pantalla crea (`alpha caused saveLayer`).

### Antes · hilo principal (gfxinfo, ms)

| Dirección | Cuadros | Lentos > 16 ms | p90 | p99 | Máx |
|---|---|---|---|---|---|
| Biblioteca → Buscar | 106 | 45 (42 %) | 26.6 | 33.5 | 33.8 |
| Biblioteca → Inicio | 111 | 38 (34 %) | 21.3 | 23.7 | 27.6 |
| Buscar → Biblioteca | 106 | 47 (44 %) | 24.9 | 63.4 | 72.1 |
| Inicio → Biblioteca | 113 | 40 (35 %) | 23.7 | 28.2 | 95.5 |
| Inicio → Buscar (control) | 113 | 20 (18 %) | 20.7 | 26.0 | 28.2 |
| Buscar → Inicio (control) | 114 | 36 (32 %) | 22.7 | 40.2 | 49.8 |

**Qué mostró el trazo al entrar a Biblioteca:**
- **Hilo principal:** el primer cuadro trae 15–18 ms de trabajo propio, parecido a salir. No hay un solo cuadro largo de forma constante. Gfxinfo sí muestra picos de 72–96 ms al entrar, en 3 de 20 entradas, siempre en el 3.er cuadro.
- **Hilo de render:** es lo que sí distingue la entrada. Cada cuadro de la animación de entrada crea **5–7 capas fuera de pantalla**. Una es de pantalla completa, por el fundido (1440×3120). Las demás son **una por cada fila** de la cascada (1440×224). Al salir de Biblioteca son 2–4 por cuadro.
- **Conclusión:** la hipótesis del cuadro pesado de medida y colocación **no se confirmó en el emulador**. Lo medible fue el costo de GPU de la cascada y que Biblioteca rehace sus capas cada vez que vuelve a colocarse.

## Paso 2 · Corrección (`4719421`)
1. **Biblioteca oculta sigue medida y colocada**, con alfa 0.
   - Ya no rehace sus capas al volver.
   - Mientras está oculta se le quita la semántica (`clearAndSetSemantics`) y una capa encima se lleva los toques.
   - **Comprobé en el emulador:** en Buscar, `uiautomator dump` solo muestra «Próximamente» y «Buscar». Un toque donde estaría una fila no reproduce nada (sesión de medios en `NONE`).
2. **DEC-28 `arranque_en_primer_cuadro`:** el fundido de Biblioteca, la cascada y la píldora esperan un `withFrameNanos` antes de empezar. El cuadro que dibuja el cambio ya no les consume tiempo. La píldora sigue siendo interrumpible: usa `Animatable`, que parte de su valor y velocidad actuales.
3. **Filas de la cascada con `CompositingStrategy.ModulateAlpha`:** sin capa fuera de pantalla por fila.

### Antes / después · hilo de render al entrar (atrace)

| | Antes | Después |
|---|---|---|
| Capas fuera de pantalla por cuadro de entrada | 5–7 | 1–3 |
| `DrawFrame` Buscar → Biblioteca: mediana / máx | 96.1 / 139.6 ms | 84.3 / 114.0 ms |
| `DrawFrame` Inicio → Biblioteca: mediana / máx | 87.3 / 162.9 ms | 75.8 / 108.8 ms |

### Después · hilo principal (gfxinfo, ms)

| Dirección | p90 | p99 | Máx |
|---|---|---|---|
| Buscar → Biblioteca | 25.5 | 51.5 | 92.5 |
| Inicio → Biblioteca | 24.4 | 35.1 | 106.7 |
| Inicio → Buscar (control) | 22.9 | 40.8 | 71.0 |

- En este emulador gfxinfo es casi todo ruido: los cuadros de control también tienen picos.
- Hubo un cuadro de 116 ms de `measureAndLayout` en la primera entrada tras arrancar la app. En las **20 entradas siguientes no se repitió** (máx. 8 ms). Parece algo puntual del arranque (JIT o primer uso), no de la corrección.
- **Brian confirmó en el Redmi que la mejora funciona.**

## Paso 3 · Texto de la píldora (`a0d6376`)
- La etiqueta aparece entre el **30 % y el 100 %** del progreso de su pestaña.
- `RolaMotion.resortePildora()`: amortiguación 0.8 y rigidez 300 (antes 400). Se asienta al 1 % en ≈ 0.33 s, dentro de los ~350 ms que permite DEC-28. Solo lo usa la píldora; el rebote del ícono sigue con `resorte()`.
- **Con reducir movimiento:** el progreso salta de 0 a 1, así que el texto también salta.

## Corrección fina tras la revisión (`e4bc071`)
- **Problema que encontró `ponytail-review`:** al quedar colocada, Biblioteca oculta podía recibir foco de teclado. Con Tab y Enter se podía reproducir una canción invisible. Antes no pasaba, porque un nodo sin colocar no recibe foco.
- **Arreglo:** mientras está oculta, `focusProperties { onEnter = { cancelFocusChange() } }.focusGroup()`.

## Paso 4 · Comprobaciones
- `.\gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` → **BUILD SUCCESSFUL**.
- **Unitarias:** 22, 0 fallas.
- **Compose** (`connectedDebugAndroidTest`, emulador): **11, 0 fallas**.
- **Lint:** 0 errores y los 2 avisos de siempre (`VectorRaster` y `ExportedService`).
- **Redmi:** le instalé el `perfil` final en el usuario principal. El permiso de audio ya estaba concedido.
- **Video:** `ai/handoff/capturas/t02e-entrar-biblioteca.mp4`, del **emulador**, no del Redmi. Como el Redmi no acepta toques por adb, no pude grabar ahí. Muestra entradas a Biblioteca desde Buscar e Inicio, con 10 canciones.
- **Nota del entorno:** la primera corrida de Compose falló al instalar (`DELETE_FAILED_INTERNAL_ERROR`) porque el emulador estaba al 95 % de espacio. Pasó después de borrar las copias de canciones.

## Dudas y pendientes para el equipo
1. **Redmi y adb:** con «Depuración USB (ajustes de seguridad)» activada, MIUI sigue rechazando `input tap` y `pm grant`. Puede hacer falta la cuenta Mi, una SIM o reiniciar el teléfono. Mientras tanto, las medidas y los videos salen del emulador, que no se parece al Redmi.
2. **Video del Redmi:** si Brian lo graba a mano (`adb shell screenrecord` mientras toca), queda en el mismo nombre.
3. **Para el grafo:** propongo anotar junto a DEC-03/DEC-28 que la raíz de Biblioteca queda colocada aunque esté oculta, sin semántica, sin toques y sin foco. Así una tarea futura no la vuelve a quitar de la composición.
4. **Prueba faltante:** no hay prueba automática de que la lista oculta no reciba toques. `RolaNavHost` necesita un `AppContainer` real. Lo comprobé a mano. Propongo cubrirlo con Maestro cuando una tarea lo pida.

## Commits
- `4719421` · perf(navegacion): entrada a Biblioteca sin cuadro pesado
- `a0d6376` · fix(motion): texto de la píldora más apreciable
- `e4bc071` · fix(navegacion): Biblioteca oculta no recibe foco de teclado
- (este) docs(handoff): reporte de la tarea 02e
