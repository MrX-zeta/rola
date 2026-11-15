# Tarea 03b · Correcciones del reproductor (probado por Brian en el Redmi)

**Estado:** esperando revisión
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.6.0 · **Spec Kit:** corrección sobre `specs/002-reproductor-completo/` con `/speckit-converge`.
**Ponytail:** off en el Paso 1; full del Paso 2 en adelante.
**Dispositivo:** si el Redmi aparece en adb, instálale `perfil` al final y avísale a Brian.

## Lectura obligatoria
- Grafo: DEC-04 y DEC-24 (con sus «CAMBIOS 2026-11-14»), DEC-16 (`timeSmall`, botón sin portada en claro), DEC-27 (portada plana), DEC-28, INV-06.
- Reporte de la tarea 03: `git show a31906f:ai/handoff/reporte.md`.
- Figma: el componente Mini reproductor (5:51) ya no tiene botón siguiente.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 03b`. Si no, detente.

## Paso 1 · Actualizar la spec 002 y `/speckit-converge`
- Agrega a `spec.md` los cambios de DEC-04 y DEC-24 de abajo como requisitos nuevos o corregidos, y las respuestas a tus dudas. Luego `/speckit-converge` para que agregue lo pendiente a `tasks.md`. Commit: `docs(spec): 002 correcciones de la revisión`.
- Detente y pide a Brian que cambie Ponytail a **full**.

## Paso 2 · Lo que encontró Brian (corregir todo)
1. **Gesto de la mini tarjeta:** hoy solo funciona empezando muy a la orilla; en medio solo rebota. El umbral debe contar la **distancia recorrida desde donde empieza el dedo** (≈ 25 % del ancho o ~96 dp, lo que sea menor) o la velocidad (~400 dp/s), empezando en cualquier punto. Calibra y deja los valores en `RolaMotion` o junto al gesto.
2. **Reproduciendo se queda a medias al deslizar hacia abajo** (la capa queda atorada sobre Biblioteca). Al soltar debe terminar siempre en cerrado o abierto, también si el gesto se cancela o lo interrumpe otro toque. Agrega una prueba que lo cubra.
3. **Reproduciendo: controles pequeños y muy arriba, hueco abajo.** Reparte la altura: portada que se adapta al alto disponible, controles más grandes (reproducir ≥ 80 dp, anterior/siguiente ≥ 56 dp como Figma) y la fila de acciones cerca del borde inferior respetando la barra del sistema.
4. **Gesto horizontal sobre la portada en Reproduciendo** para siguiente/anterior, con el mismo umbral que la mini tarjeta y una respuesta visual (la portada se desplaza con el dedo y vuelve o cambia).
5. **Mini tarjeta:** quita el botón siguiente; deja solo reproducir/pausa. El título corre como **marquesina** solo si no cabe (Compose `basicMarquee`); si cabe o hay reducir movimiento, queda estático con puntos suspensivos.
6. **Cola:** tocar una fila reproduce esa canción. Al arrastrar cerca del borde superior o inferior, la lista se desplaza sola (las dos direcciones), y una fila arrastrada nunca se «come» ni se pierde al llegar al tope: se coloca en la posición válida más cercana.

## Paso 3 · Respuestas a tus dudas de la tarea 03
1. Portada de Reproduciendo: **plana** (DEC-27).
2. «Limpiar» en la Cola: **sí**. Quita todo lo que sigue y deja la actual, con aviso y «Deshacer».
3. Sin portada en claro: el botón de reproducir usa **accent** (lima) con `onAccent`; barra y título siguen en `primary` por contraste.
4. Mini tarjeta en claro con la variante clara del color: **confirmado**.
5. `timeSmall` ya está en DEC-16. `repeat_one` lo agrega la sesión de diseño a Figma.
También corrige:
- **Portada borrosa:** pide la portada al tamaño en que se dibuja (no 512 px fijo).
- **Órdenes antes de conectar:** que el primer toque tras un arranque en frío no se pierda (guárdalas y envíalas al conectar). Quita el `shortcut:` correspondiente.

## Paso 4 · Comprobaciones y reporte
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- Pruebas nuevas: umbral desde el centro de la tarjeta, cierre que nunca queda a medias, tocar fila de la cola, «Limpiar» + deshacer, marquesina estática con reducir movimiento.
- Capturas: `t03b-reproduciendo.png` (oscuro y claro), `t03b-mini.png`, `t03b-cola.png`.
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
