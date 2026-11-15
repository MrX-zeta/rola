# Tarea 03c · Arrastre de la cola sin fugas

**Estado:** esperando revisión
**Escrita:** 2026-11-15 · sesión de diseño
**Rama:** `development`. En worktree: actualízalo con `origin/development` antes de empezar y al final sube tu rama a `origin development`.
**Grafo:** 1.6.1 · **Spec Kit:** corrección sobre `specs/002-reproductor-completo/` con `/speckit-converge`.
**Ponytail:** full en todos los pasos (el Paso 1 es solo agregar dos requisitos).
**Dispositivo:** si el Redmi aparece en adb, instálale `perfil` al final.

## Lo que vio Brian en el Redmi
1. **Arriba:** al subir una canción hasta el límite superior (debajo del encabezado «Cola»), la fila «se la come» y se sale de su dedo.
2. **Abajo:** al llevar una canción hasta el final, además del autodesplazamiento la fila sigue bajando y solo se detiene cuando suelta.
Todo lo demás de la 03b quedó aprobado.

## Causas probables (confírmalas)
- **Arriba:** cuando la fila arrastrada pasa a ser la primera visible, `LazyColumn` mantiene anclado el primer elemento visible por clave y la lista «salta»; además el desplazamiento del dedo no compensa lo que se movió la lista. Arreglo conocido: al intercambiar con el primer elemento visible, conservar la posición (`scrollToItem(firstVisibleItemIndex, firstVisibleItemScrollOffset)`) y sumar al desplazamiento de la fila lo que se desplazó la lista.
- **Abajo:** el autodesplazamiento no se detiene al no poder avanzar (`canScrollForward`) y el desplazamiento de la fila no está limitado al último índice.

## Paso 0 · Comprobación
- `git log --oneline -3` incluye `docs: sesión de diseño tarea 03c`. Si no, detente.

## Paso 1 · Spec
- Agrega a `spec.md` los dos casos como requisitos (DEC-24: «la fila se queda siempre bajo el dedo» y «el autodesplazamiento se detiene en el inicio y el final; la fila nunca sale de los límites»). `/speckit-converge`. Commit: `docs(spec): 002 arrastre de la cola`.

## Paso 2 · Corregir
- Arregla los dos casos con las causas que confirmes.
- **Alternativa permitida:** si el arrastre propio no queda sólido, cambia a la librería `sh.calvin.reorderable:reorderable` (Apache 2.0, sin red), que resuelve estos bordes; justifícalo en el reporte y conserva las acciones de accesibilidad «Subir», «Bajar» y «Quitar de la cola».
- **Marquesina (DEC-04):** corre mientras la canción suena, con 2 s de pausa entre pasadas; en pausa, estática. Con reducir movimiento, siempre estática.
- Commit: `fix(player): arrastre de la cola sin fugas` (y `fix(player): marquesina mientras suena`).

## Paso 3 · Comprobaciones y reporte
- Prepara en el emulador una cola larga (≥ 40 canciones; copias de los audios de prueba, bórralas al terminar) para probar el autodesplazamiento.
- Pruebas: la fila arrastrada al primer lugar queda bajo el dedo y en índice 0; arrastrada más allá del final queda en el último índice y el autodesplazamiento se detiene; marquesina estática en pausa.
- `./gradlew :app:assembleDebug :app:assemblePerfil :app:testDebugUnitTest :app:lintDebug` y las de Compose.
- Video corto `t03c-cola-arrastre.mp4` (arriba y abajo).
- `/ponytail:ponytail-review`, `git log` sin autoría de IA, push a `origin development`. Reporte y estado «esperando revisión».
