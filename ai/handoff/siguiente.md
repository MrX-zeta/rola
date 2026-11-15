# Tarea 02e · Entrada a Biblioteca sin saltos (medido en el Redmi)

**Estado:** lista
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.4.3 · Sin Spec Kit.
**Ponytail:** full en todos los pasos.
**Dispositivo:** el Redmi conectado por USB, con «Depuración USB (ajustes de seguridad)» activada por Brian. Si `adb shell input tap` sigue rechazado, detente y avisa.

## Lo que vio Brian en el Redmi (tipo `perfil`, 375 canciones, 120 Hz)
- Salir de Biblioteca ya va bien, e Inicio ↔ Buscar es fluido.
- **Entrar a Biblioteca** (desde Inicio o Buscar): la píldora no se anima, «se pinta» directo en su lugar, y **no se ve la cascada** de las canciones.
- Hipótesis de la sesión de diseño: en el Redmi el primer cuadro de Biblioteca visible es pesado (medir y colocar las filas, portadas). La píldora y la cascada empiezan a contar en el toque o en el cambio de ruta; cuando ese cuadro largo termina, ya «pasó» su tiempo y aparecen terminadas. **Confírmalo midiendo antes de cambiar.**

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 02e`. Si no, detente.

## Paso 1 · Medir en el Redmi
- Mismos scripts de la 02d (`gfxinfo` y atrace), en el Redmi y con `perfil`: Inicio→Biblioteca, Buscar→Biblioteca, Biblioteca→Buscar e Inicio↔Buscar como control. 10 cambios cada uno.
- Busca el cuadro largo al entrar a Biblioteca y qué lo ocupa. Tabla en el reporte antes de cambiar código.

## Paso 2 · Corregir (DEC-28 `arranque_en_primer_cuadro`)
- **Que entrar a Biblioteca sea barato:** si el costo es medir/colocar la lista al volverse visible, mantenla medida y colocada mientras está oculta (oculta con `alpha`/fuera de semántica y sin recibir toques), o la alternativa que la medida indique. Que el cuadro de entrada sea comparable al de Inicio ↔ Buscar.
- **Que las animaciones no se coman su tiempo:** la cascada y la píldora empiezan a contar en el primer cuadro en que Biblioteca ya está dibujada (p. ej. esperar un `withFrameNanos` tras volverse visible), no en el toque ni en el cambio de ruta.
- Repite la medida y pon antes/después en el reporte.
- Commit: `perf(navegacion): entrada a Biblioteca sin cuadro pesado` (o el nombre que corresponda a la causa).

## Paso 3 · Aparición del texto en la píldora
- Brian quiere apreciar un poco más la aparición del texto. Permitido llegar a ~350 ms solo en la píldora (DEC-28). Sugerencia: el texto empieza a aparecer a partir de ~30 % del progreso y termina con él, y el resorte un poco más suave. Sin que la píldora se sienta lenta ni pierda la interrupción.
- Con reducir movimiento todo sigue saltando.
- Commit: `fix(motion): texto de la píldora más apreciable`.

## Paso 4 · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- Graba en el **Redmi** (`adb shell screenrecord`) `t02e-entrar-biblioteca.mp4`. Instálale el `perfil` final.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte con tablas antes/después y estado «esperando revisión».
