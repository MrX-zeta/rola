# Tarea 01b · Spec Kit, tokens nuevos y pendientes de la 01

**Estado:** lista
**Escrita:** 2026-11-14 · sesión de diseño
**Rama:** `development`. Si trabajas en un worktree, al final sube tu rama a `origin development` como en la tarea 01.
**Grafo:** 1.2.1

## Antes de empezar (lo hace Brian)
- [x] `uv` y `specify` instalados (Git Bash).
- [ ] Commit y push de la sesión de diseño (ver «Paso 0»). Sin esto, un worktree no ve esta tarea.
- [ ] Ponytail en **off** durante el Paso 1; en **full** desde el Paso 2.

## Lectura obligatoria
- Grafo: INV-01…INV-11, DEC-16 (tokens `accent`/`onAccent` y roles M3 derivados), DEC-17, DEC-27 (relieve, oscuro y claro), PEND-06.
- `ai/handoff/revisiones-pendientes.md` y el reporte de la tarea 01 (está en el historial: `git show ec6c4a9:ai/handoff/reporte.md`).

## Paso 0 · Comprobación
- `git log --oneline -3` debe mostrar el commit `docs: sesión de diseño 2026-11-14`. Si no está, o si este archivo no dice «Tarea 01b», detente y avisa en el reporte.

## Paso 1 · Spec Kit (PEND-06)
- Corre `specify --help` y `specify init --help` y anota en el reporte las opciones reales de tu versión.
- Inicializa en la raíz: `specify init --here --ai claude --script sh` (si tu versión usa `--integration`, usa esa opción). Scripts **sh**: en Windows Claude Code y Brian usan Git Bash.
- Si `init` quiere reemplazar `CLAUDE.md` u otro archivo existente, no lo sobrescribas: conserva el nuestro y agrega solo lo necesario.
- `/speckit.constitution` con artículos cortos en español que citen ids del grafo: INV-01…INV-11, DEC-01 (sin conexión), DEC-20 (arquitectura), DEC-21 (forma de trabajo), DEC-22 (pruebas), DEC-27 (estilo). Debe decir que si una spec contradice al grafo, manda el grafo.
- PEND-06: lista en el reporte los comandos `/speckit.*` disponibles y si alguno sirve para corregir errores. No edites el grafo.
- Commit: `chore(speckit): inicialización y constitución`.

## Paso 2 · Colores nuevos (DEC-16)
- `accent` = `#C7FF2E` y `onAccent` = `#10100E`, **iguales en claro y oscuro**: relleno de acciones principales (botón principal, pestaña activa, chip activo, botón de reproducir). `ColorScheme` de M3 no tiene ese rol: crea `RolaColors` (data class) con `accent` y `onAccent`, expuesto con un `CompositionLocal` y `RolaTheme.colors`.
- Completa los roles M3 que quedaron derivados o por defecto con los valores de `DEC-16.detalles.roles_m3_derivados` (incluido `error`).
- `BrandTokensTest`: agrega `accent` y `onAccent` en ambos temas. `ContrastTest`: `onAccent` sobre `accent` ≥ 4.5.
- Muestrario: agrega `accent`.
- Commit: `feat(designsystem): color accent y roles M3 completos`.

## Paso 3 · Relieve (DEC-27)
- `RolaRelief` en `core/designsystem` con tres modificadores: `elevado`, `hundido`, `acento`. Cada uno con su versión oscura y clara (valores en DEC-27: `detalles.efectos` y `efectos_claro`) elegida según el tema.
- Usa `Modifier.dropShadow` e `innerShadow` de Compose (si tu versión de la BOM no los trae, dilo en el reporte y usa `drawBehind`; no agregues librerías).
- Solo se usarán en onboarding, Sin canciones, Buscar y Sin letra. No los apliques a nada todavía; muéstralos en el Muestrario.
- Commit: `feat(designsystem): relieve acotado`.

## Paso 4 · Splash siempre oscuro (DEC-17)
- `splash_fondo` = `#10100E` también en `values/` (quita la diferencia con `values-night/`). El círculo lima no cambia.
- Comprueba en el emulador con el sistema en **claro** que ya no hay salto de claro a oscuro. Captura: `t01b-splash-sistema-claro.png`.
- Commit: `fix(marca): splash oscuro mientras no haya ajuste de tema`.

## Paso 5 · Configuración (INV-03)
- Agrega `local.properties` a `permissions.deny` en `.claude/settings.json`.
- Commit: `chore(claude): denegar lectura de local.properties`. **REQUIERE REVISIÓN HUMANA.**

## Paso 6 · Comprobaciones
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (Git Bash). Anota resultados reales.
- `/ponytail:ponytail-review` sobre todo lo de esta tarea antes del último commit.
- Revisa con `git log` que ningún mensaje tenga autoría de IA. Push a `origin development`. Sin PR.

## Paso 7 · Reporte y detente
- `ai/handoff/reporte.md` con el formato de siempre. Estado de esta tarea: «esperando revisión». No empieces la tarea 02.

## Fuera de esta tarea
- Pantallas reales, reproducción, Room, navegación: llegan en la tarea 02.
