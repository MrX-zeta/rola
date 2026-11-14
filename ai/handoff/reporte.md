# Reporte · Tarea 01b · Spec Kit, tokens nuevos y pendientes de la 01

**Estado:** esperando revisión (falta la captura del Paso 4: el emulador no respondió).
**REQUIERE REVISIÓN HUMANA:** `.claude/settings.json` (Paso 5).

## Paso 0 · Comprobación
- El worktree estaba en `b0b8691` con la tarea 01 en `siguiente.md`. Con tu permiso lo adelanté a `origin/development` (`b08ed9a`, «docs: sesión de diseño 2026-11-14») y ahí aparece «Tarea 01b».

## Paso 1 · Spec Kit (PEND-06)
- `specify` ofrece: `init`, `check`, `version`, `mcp`, `self`, `extension`, `integration`, `preset`, `event`, `artifact`, `bundle`, `workflow`.
- `specify init` usa `--integration` (no `--ai`), con `--here`, `--script sh|ps`, `--force`, `--non-interactive`, `--preset`, `--extension`.
- Corrí `specify init --here --integration claude --script sh --non-interactive --force`. No tocó ningún archivo versionado (`CLAUDE.md` y `.gitignore` intactos); solo agregó `.specify/` y `.claude/skills/speckit-*`.
- **Comandos disponibles** (instalados como skills, con guion): `/speckit-constitution`, `-specify`, `-clarify`, `-plan`, `-tasks`, `-analyze`, `-checklist`, `-implement`, `-converge`.
- **PEND-06:** no existe comando de bug/fix. El más cercano es `/speckit-converge` (compara el código con spec, plan y tareas y agrega lo pendiente a `tasks.md`). Para un error: especificación corta + `converge`.
- Constitución escrita a mano en `.specify/memory/constitution.md` (11 artículos, citan INV-01…11 y DEC-01/20/21/22/27; el grafo manda). No pasó por `/speckit-constitution`.

## Paso 2 · Color accent
- `RolaColors(accent, onAccent)` + `LocalRolaColors` + `RolaTheme.colors` (`core/designsystem/Color.kt`, `Theme.kt`).
- Roles M3: `error*` claro/oscuro y `surfaceTint` transparente (los demás ya coincidían con DEC-16).
- Pruebas: `accent`/`onAccent` en `BrandTokensTest`; `onAccent/accent ≥ 4.5` en `ContrastTest`. Muestrario con accent y error.
- Nota: `RolaColors` es igual en ambos temas, así que la prueba lee el valor por defecto, no un esquema por tema.

## Paso 3 · Relieve
- `Relief.kt`: `Modifier.elevado/hundido/acento(shape)` con `dropShadow` e `innerShadow` (la BOM 2026.02.01 los trae; sin `drawBehind` ni librerías).
- Dark: valores exactos de DEC-27. **Claro: DEC-27 solo da rangos**; usé negro 12 % (16 % en acento) y luz 85 %. Falta confirmar contra Figma.
- No se aplican a ninguna pantalla; se muestran en el Muestrario. **No los vi renderizados.**
- Ponytail-review: quité un `object RolaRelief` vacío que yo había creado.

## Paso 4 · Splash siempre oscuro
- `splash_fondo` = `#10100E` en `values/`; borré `values-night/colors.xml`.
- **No hecho:** `t01b-splash-sistema-claro.png`. El emulador `Pixel_7_Pro` arrancó pero `adb shell` no respondía (timeout), lo cerré. Pendiente de verificar en teléfono o emulador sano.

## Paso 5 · Configuración (INV-03)
- `local.properties` y `**/local.properties` en `permissions.deny`.
- Este worktree no tenía `local.properties`; compilé con `ANDROID_HOME` (sin leer el archivo).

## Comprobaciones reales
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL.
- Pruebas: 6 (3 + 3), 0 fallas, 0 omitidas.
- Lint: 0 errores, 3 avisos (AndroidGradlePluginVersion, GradleDependency, NewerVersionAvailable; versiones nuevas disponibles).
- Capturas: ninguna de esta tarea (ver Paso 4).
- `git log`: sin líneas de autoría de IA.

## Para el equipo
1. Brian: ¿captura del splash con sistema en claro en tu teléfono?
2. Confirmar en Figma los valores claros de relieve (DEC-27 `efectos_claro`).
3. Grafo: PEND-06 resuelto (no hay comando de errores; usar `converge`); los comandos de Spec Kit son `/speckit-*`, con guion, no `/speckit.*`. Actualizar CLAUDE.md y `ai/handoff/README.md` cuando se decida.

## Commits
- 7f1258d · chore(speckit): inicialización y constitución
- 40a21ed · feat(designsystem): color accent y roles M3 completos
- 8b464c1 · feat(designsystem): relieve acotado
- 6096a08 · fix(marca): splash oscuro mientras no haya ajuste de tema
- ee8ccdb · chore(claude): denegar lectura de local.properties
- 74a6bf6 · refactor(designsystem): quitar objeto RolaRelief vacío
