# Reporte · Tarea 01 · Fundaciones: herramientas, Claude Code, tema y marca

**Estado:** esperando revisión (todo hecho excepto Spec Kit, bloqueado: ver Paso 2).
**REQUIERE REVISIÓN HUMANA:** `.claude/settings.json`, `.githooks/commit-msg` y `.gitattributes` (configuración de herramientas).

## Paso 0 · Rama y commit inicial
- `ai/`, `docs/marca/` y `.gitignore` ya estaban confirmados en `main` (commit `b0b8691`), así que no hubo commit de este paso.
- `development` ya existe y apunta a ese mismo commit, pero está abierta en el checkout principal. Trabajé en la rama de este worktree (`claude/rola-android-player-2983e3`) y la subo a `origin development` (ver Commits).

## Paso 1 · Configuración de Claude Code
- `CLAUDE.md` (69 líneas): qué es Rola, dónde está la verdad, ciclo de handoff, INV-01…INV-11, comandos de Gradle, convenciones, reglas de commit, tabla Spec Kit/Ponytail y el aviso de `git config core.hooksPath .githooks` al clonar.
- `.claude/settings.json`: `attribution` vacía y `sessionUrl: false`; `permissions.deny` de lectura para `keystore.properties`, `*.jks`, `*.keystore`, `*.p12` y `.env*`.
- `.githooks/commit-msg`: borra `Co-Authored-By` con «anthropic», `Claude-Session:` y «Generated with … Claude». Probado a mano con un mensaje de ejemplo (quita los tres y deja un `Co-Authored-By` de una persona). Activado con `git config core.hooksPath .githooks`.
- `.gitattributes` (no estaba en la tarea): fuerza LF en `.githooks/*`. Sin él, Git en Windows lo convierte a CRLF y el gancho falla en Linux/macOS.

## Paso 2 · Spec Kit — BLOQUEADO
- `uv` y `specify` no están instalados en esta máquina (el punto 2/3 de «Antes de empezar» no se hizo). No los instalé por mi cuenta.
- No se creó `.specify/`, no hay comandos `/speckit.*` ni constitución. No hay commit `chore(speckit)`.
- **PEND-06:** sin la CLI no puedo listar los comandos de mi versión ni saber si existe uno para corregir errores. Queda sin responder.
- Para desbloquear: `winget install astral-sh.uv`, luego `uv tool install specify-cli --from git+https://github.com/github/spec-kit.git`, y repetir el Paso 2 en una tarea corta.
- Ponytail: no está instalado como plugin (no aparece en `installed_plugins.json`), pero el modo y la skill `ponytail:ponytail-review` estaban disponibles en la sesión y los usé.

## Paso 3 · Gradle
- `minSdk` 30; `compileSdk`/`targetSdk` 37; `versionName` 0.1.0; `versionCode` 1; `app_name` «Rola».
- Versiones consultadas en Google Maven (estables, sin alpha/beta/rc): core-ktx 1.19.1, lifecycle-runtime-ktx 2.11.0, activity-compose 1.13.0, androidx.test.ext:junit 1.3.0, espresso-core 3.7.0.
- Agregado solo `androidx.core:core-splashscreen` 1.2.0. No toqué AGP (9.4.1) ni la BOM de Compose (2026.02.01).

## Paso 4 · Tema (`core/designsystem`)
- `Color.kt`: `RolaDarkColors` y `RolaLightColors` con los hex de DEC-16; sin color dinámico. Único lugar con hex en Kotlin.
- `Type.kt`: Space Grotesk Bold para display, headline y titleLarge; Geist para el resto; `Typography.time` con Geist Mono Medium.
- `Shape.kt` (12/20/24/28 + `Shapes.pill`), `Spacing.kt` (`RolaSpacing`: 4, 8, 12, 16, 20, 24, 32), `Theme.kt` (`RolaTheme(darkTheme = true)`), `Contrast.kt` (`contrastRatio`).
- Fuentes **variables** de `google/fonts` (GitHub) en `res/font/` (`space_grotesk.ttf`, `geist.ttf`, `geist_mono.ttf`), usadas con `FontVariation` (API experimental, con `@OptIn`). Sus `OFL.txt` están en `docs/legal/licencias/`.
- `Muestrario` (`@Preview`) vive en `app/src/debug/`, así que no entra al APK de release. **No lo abrí en Android Studio:** solo compila.

## Paso 5 · Ícono y splash
- `ic_launcher_foreground.xml` y `ic_launcher_monochrome.xml` (Vector Drawable, trazo redondeado y `evenOdd`), `ic_launcher_background` = `#C7FF2E`; `ic_launcher` y `ic_launcher_round` usan la monocroma nueva. Borrados los `.webp` y el fondo vectorial de la plantilla.
- Splash: `Theme.Rola.Starting` hereda de **`Theme.SplashScreen.IconBackground`** (no del `Theme.SplashScreen` a secas). Con el padre simple, `windowSplashScreenIconBackgroundColor` se ignora: en la primera prueba no salía el círculo lima y en oscuro el ícono (tinta `#10100E`) quedaba invisible sobre `#10100E`. `Theme.SplashScreen.IconBackground` hereda de `Theme.SplashScreen`.
- `Theme.Rola`: hereda de `android:Theme.DeviceDefault.DayNight`, sin barra de título, con `windowBackground` = `splash_fondo` (`#F6F6F4` / `#10100E` en `values-night`).
- Revisión a 48 dp: no la hice a 48 dp. En el launcher del emulador (capturas) se ven la «r» y el agujero del disco.

## Paso 6 · Pantalla temporal
- `MainActivity`: `installSplashScreen()` antes de `super.onCreate`, `enableEdgeToEdge`, fondo `background`, «rola» en `displayLarge` y «Fundaciones listas» en `bodyMedium`/`onSurfaceVariant`. Textos en `strings.xml`.
- `enableEdgeToEdge` lleva `SystemBarStyle.dark` fijo, porque la app está fija en oscuro. Sin eso, con el sistema en claro los íconos de las barras salían oscuros sobre fondo oscuro. Hay que quitarlo cuando llegue Ajustes.

## Paso 7 · Pruebas
- Reemplacé `ExampleUnitTest` por `ContrastTest` (2 pruebas) y `BrandTokensTest` (2). Borré `ExampleInstrumentedTest` y no puse otra de instrumentación (la tarea no la pedía).
- Contrastes medidos (texto/fondo): onSurface/background 17.9 (oscuro) · 17.6 (claro); onSurfaceVariant/surface 7.1 · 6.9; onSurfaceVariant/surfaceContainer 6.3 · 5.8; primary/background 16.1 · 8.1; onPrimaryContainer/primaryContainer 11.6 · 16.1. Todos ≥ 4.5.

## Comprobaciones reales
Corridas juntas con `.\gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (Windows, con `ANDROID_HOME` apuntando al SDK; el worktree no tiene `local.properties`, que no leo):
- `:app:assembleDebug`: BUILD SUCCESSFUL.
- `:app:testDebugUnitTest`: 4 pruebas, 0 fallas, 0 omitidas (2 + 2).
- `:app:lintDebug`: 0 errores, 3 avisos: `AndroidGradlePluginVersion` (hay AGP 9.5.1), `GradleDependency` y `NewerVersionAvailable` (hay versiones más nuevas de alguna dependencia). No son de código nuestro. Quité el aviso `RedundantLabel` de la plantilla.
- No corrí `assembleRelease`.
- Emulador Pixel 7 Pro, Android 16 (API 36). Capturas en `ai/handoff/capturas/`:
  - `t01-splash-oscuro.png`, `t01-splash-claro.png`: el splash dura menos que una captura, así que para tomarlas retuve el splash 4 s en un build local temporal y lo revertí; el código confirmado no tiene demora.
  - `t01-icono-launcher.png` (recorte del launcher; el halo claro es el anillo de foco del launcher).
  - `t01-pantalla-temporal.png`.
  - **Falta `t01-icono-tematico.png`:** el launcher del emulador no activó los íconos temáticos con `settings put secure themed_icon_enabled`. La capa monocroma está declarada y compila, pero no la vi renderizada. Hay que verla en el teléfono de Brian.

## Revisión (`ponytail-review`)
- Sin hallazgos que corregir. No apliqué nada porque no había nada que aplicar.
- Aun así lo dejo anotado para ustedes: (a) los roles de M3 que DEC-16 no define (secondary, tertiary, surfaceContainerLow/High/Highest, inverse…, surfaceTint) los derivé de los mismos tokens; `error` queda con los valores por defecto de M3; (b) `permissions.deny` no incluye `local.properties` porque la tarea no lo pedía, aunque INV-03 y `CLAUDE.md` sí lo prohíben.
- `design-critique` contra Figma: **no lo hice**; la pantalla temporal no tiene nodo en Figma (SCR-10 llega después).

## Para el equipo
1. **Spec Kit sigue sin instalar** (ver Paso 2) y PEND-06 sin respuesta.
2. **Splash claro y app oscura:** con el sistema en claro el splash sale `#F6F6F4` y luego la app abre en oscuro (`RolaTheme(darkTheme = true)` por defecto, como pidió la tarea). Hay un cambio brusco de claro a oscuro. Se arregla cuando Ajustes permita Sistema/Oscuro/Claro; si molesta antes, se puede fijar el splash siempre oscuro.
3. **Estado final:** la tarea pedía «hecha»; el prompt de Claude Code pedía «esperando revisión». Dejé «esperando revisión» porque hay cosas marcadas para revisión humana.
4. **Propuestas al grafo:**
   - DEC-17: anotar que el tema del splash debe heredar de `Theme.SplashScreen.IconBackground` para que el círculo lima se dibuje.
   - DEC-16 o DEC-15: definir los roles de M3 sin hex propio (secondary, tertiary, error, etc.) en lugar de dejarlos derivados.
   - INV-03: agregar `local.properties` a la lista de lecturas denegadas en `.claude/settings.json`.
   - PEND-06: sigue pendiente hasta instalar Spec Kit.

## Commits
Hechos en la rama `claude/rola-android-player-2983e3` (el worktree no puede abrir `development`, que está en el checkout principal); se suben a `origin development`.
- `0a4696c` · chore(claude): CLAUDE.md, ajustes y gancho de commits
- `576c56a` · build: minSdk 30, versiones y splashscreen
- `66345ae` · feat(designsystem): colores, tipografía, formas y espacios de Rola
- `114f222` · feat(marca): ícono adaptativo, splash y pantalla temporal
- `f4eb8fc` · test(designsystem): contraste y tokens de marca
- (ver git log) · docs(handoff): reporte de la tarea 01
- No hay commit `chore(speckit)`.
