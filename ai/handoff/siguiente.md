# Tarea 01 · Fundaciones: herramientas, Claude Code, tema y marca

**Estado:** lista
**Escrita:** 2026-11-13 · sesión de diseño
**Rama:** `development` (créala desde `main` si no existe)

Es la primera tarea: deja el proyecto listo para trabajar con Spec Kit y Ponytail, con la identidad de Rola aplicada (tema, ícono y splash). **No se construyen pantallas reales todavía.**

---

## Antes de empezar (lo hace Brian, una sola vez)
1. **Node.js** instalado y en el PATH (`node -v`): lo necesitan los ganchos de Ponytail.
2. **uv** (instalador de Python): `winget install astral-sh.uv`.
3. **CLI de Spec Kit**, en una terminal: `uv tool install specify-cli --from git+https://github.com/github/spec-kit.git`.
4. **Ponytail**, dentro de Claude Code (escritorio o CLI), en **dos mensajes separados**:
   - `/plugin marketplace add DietrichGebert/ponytail`
   - `/plugin install ponytail@ponytail`
5. Abrir Claude Code en la raíz del proyecto y escribir: *«Lee ai/handoff/siguiente.md y ejecútala.»*

Si el paso 3 o el 4 no está hecho, Claude Code lo anota en el reporte y sigue con lo demás.

---

## Lectura obligatoria
- Del grafo `ai/context/knowledge-graph.json`, todos los nodos `invariante` y **DEC-02, DEC-15, DEC-16, DEC-17, DEC-20, DEC-21**. Usa Grep por id; no leas el archivo completo.
- `docs/marca/README.md`: tokens, fuentes, reglas del logo.
- `ai/handoff/README.md`: cómo reportar.

---

## Paso 0 · Commit de lo que dejó la sesión de diseño
- Crea la rama `development` desde `main`.
- Commit con `ai/`, `docs/marca/` y el `.gitignore` tal cual están: `docs: contexto del proyecto y kit de marca`.

## Paso 1 · Configuración de Claude Code (DEC-21, INV-07)
- **`CLAUDE.md`** en la raíz, en español, de menos de 150 líneas. Debe incluir:
  - qué es Rola, en 2 líneas;
  - dónde está la verdad: grafo (decisiones), Figma (visual), `docs/marca` (marca), `ai/handoff` (tareas);
  - el ciclo de `ai/handoff/README.md`;
  - los invariantes en una línea cada uno, con su id;
  - los comandos de Gradle en Windows (`.\gradlew …`);
  - las convenciones:
    - Kotlin y Compose;
    - textos de interfaz en `strings.xml` y en español;
    - nada de colores hex fuera de `core/designsystem`;
    - paquetes según DEC-20;
  - las reglas de commit: atómicos, en español, con el formato `tipo(área): descripción`, sin autoría de IA;
  - cuándo usar Spec Kit y Ponytail (la tabla de `ai/handoff/README.md`).
- **`.claude/settings.json`** (versionado):
  - `"attribution": { "commit": "", "pr": "", "sessionUrl": false }`;
  - `permissions.deny` para leer `keystore.properties`, `**/*.jks`, `**/*.keystore`, `**/*.p12` y `.env*`.
- **Respaldo por si la configuración falla:** gancho `.githooks/commit-msg` (sh) que borre las líneas `Co-Authored-By:` con `anthropic`, las `Claude-Session:` y las de «Generated with Claude Code».
  - Actívalo con `git config core.hooksPath .githooks`.
  - Esa configuración es local y no viaja con el repositorio: anota en `CLAUDE.md` que hay que correrla al clonar.
- **REQUIERE REVISIÓN HUMANA:** `settings.json` y el gancho.

## Paso 2 · Spec Kit
- Corre primero `specify init --help`. Las versiones recientes usan `--integration` y las anteriores `--ai`; usa la que exista:
  - `specify init --here --ai claude --script ps`, o
  - `specify init --here --integration claude --script ps`.
- **No sobrescribas** `.gitignore` ni `CLAUDE.md`. Si Spec Kit trae los suyos, combina el contenido.
- Comprueba que existan los comandos `/speckit.*` (en `.claude/commands/` o como skills).
- Ejecuta `/speckit.constitution` con estos principios:
  - El grafo manda. La constitución **cita** ids (INV-01…INV-11, DEC-21) en lugar de copiar decisiones.
  - Sin conexión primero (INV-01).
  - Tokens obligatorios (INV-04).
  - Accesibilidad (INV-06).
  - Retroalimentación visible (INV-05).
  - Pruebas para toda lógica nueva (DEC-22).
  - Código mínimo sin recortar seguridad ni accesibilidad (Ponytail).
  - Todo en español (interfaz, commits, documentos).
- **PEND-06:** lista los comandos que trae tu versión e indica en el reporte si existe uno para corregir errores (bug o fix), y cómo se llama.

## Paso 3 · Gradle (DEC-02)
- `minSdk = 30`. `compileSdk` y `targetSdk` se quedan en 37.
- `versionName = "0.1.0"`, `versionCode = 1`.
- Actualiza a la última versión estable las dependencias viejas de la plantilla: `core-ktx 1.10.1`, `lifecycle 2.6.1`, `activity-compose 1.8.0`, `junit-ext`, `espresso`. Comprueba qué versiones existen; no las inventes.
- Agrega **solo** `androidx.core:core-splashscreen`. Media3, Room, Glance y las demás se agregan en la tarea que las use.
- `app_name` = «Rola».

## Paso 4 · Tema en `core/designsystem` (DEC-15, DEC-16, INV-04)
- Mueve `ui/theme` a `com.luis.rola.core.designsystem` y borra los colores morados de la plantilla.
- **Color:**
  - `darkColorScheme` y `lightColorScheme` completos con los hex de DEC-16, que también están en `docs/marca/README.md` (neutros cálidos);
  - `dynamicColor` apagado: sin colores del fondo de pantalla.
  - Por ahora `RolaTheme(darkTheme = true)` por defecto: oscuro es la base. El ajuste Sistema/Oscuro/Claro llega con Ajustes.
- **Fuentes en `res/font/`**, sin el proveedor de Google Fonts descargable porque necesita red (INV-01):
  - Space Grotesk Bold; Geist Regular, Medium y SemiBold; Geist Mono Medium.
  - Descárgalas de sus repositorios oficiales (Google Fonts en GitHub o Vercel). Si solo existe la versión variable, úsala con `FontVariation`.
  - Copia cada `OFL.txt` a `docs/legal/licencias/`.
- **Tipografía M3:**
  - display, headline y titleLarge con Space Grotesk Bold;
  - el resto con Geist;
  - un estilo extra `time` con Geist Mono Medium, expuesto como extensión o `CompositionLocal`, para los tiempos.
- **Formas:** small 12, medium 20, large 24 y extraLarge 28; además `pill` totalmente redonda.
- **Espacios:** objeto `RolaSpacing` con 4, 8, 12, 16, 20, 24 y 32.
- **Muestrario:** un `@Preview` llamado «Muestrario» con colores, tipos y formas en oscuro y en claro. Solo vista previa; no va a la app.

## Paso 5 · Ícono adaptativo y splash (DEC-16, DEC-17)
- Convierte `docs/marca/ic_launcher_foreground.svg` y `ic_launcher_monochrome.svg` a Vector Drawable en `res/drawable/`.
  - Conserva el trazo redondeado de la «r» y la regla `evenodd` del disco.
  - Revísalos a 48 dp: la «r» y el agujero del disco deben verse.
- **Fondo:** color `#C7FF2E` como recurso `ic_launcher_background`.
- **`mipmap-anydpi/ic_launcher.xml` y `ic_launcher_round.xml`:** fondo, frente y la **monocroma nueva**. Hoy la plantilla usa el frente como monocroma.
- Borra los `.webp` de `mipmap-*dpi`: con minSdk 30 el ícono adaptativo basta.
- **Splash:**
  - `Theme.Rola.Starting` hereda de `Theme.SplashScreen`;
  - fondo `#F6F6F4` en `values/` y `#10100E` en `values-night/`;
  - ícono animado = el frente del ícono, con `windowSplashScreenIconBackgroundColor` = `#C7FF2E` y `postSplashScreenTheme` = `Theme.Rola`;
  - `installSplashScreen()` antes de `super.onCreate`;
  - sin demoras artificiales.
- `Theme.Rola` debe tener la ventana del color de fondo del tema para que no haya destello blanco al abrir en oscuro.

## Paso 6 · Pantalla temporal
- `MainActivity` con `enableEdgeToEdge()` y `RolaTheme`: fondo `background`, la palabra «rola» en `displayLarge` y debajo, en `bodyMedium` / `onSurfaceVariant`, «Fundaciones listas».
- Es temporal: Inicio (SCR-10) llega en otra tarea.

## Paso 7 · Pruebas (DEC-22)
- Reemplaza `ExampleUnitTest` y `ExampleInstrumentedTest`.
- Función pura `contrastRatio(a, b)` en `core/designsystem` (WCAG 2.x).
- **`ContrastTest`:** en ambos esquemas, ≥ 4.5 en onSurface/background, onSurfaceVariant/surface, onSurfaceVariant/surfaceContainer, primary/background y onPrimaryContainer/primaryContainer.
- **`BrandTokensTest`:** `primary` oscuro = `0xFFC7FF2E` y `background` oscuro = `0xFF10100E`, para detectar si alguien los cambia por error.
- **Corre:**
  - `.\gradlew :app:assembleDebug`
  - `.\gradlew :app:testDebugUnitTest`
  - `.\gradlew :app:lintDebug`
- **En el emulador o el teléfono, guarda en `ai/handoff/capturas/`:**
  - `t01-splash-oscuro.png` y `t01-splash-claro.png`;
  - `t01-icono-launcher.png`, y si el launcher lo permite, `t01-icono-tematico.png`;
  - `t01-pantalla-temporal.png`.

## Paso 8 · Revisión y commits
- Corre `/ponytail:ponytail-review` sobre el diff de la tarea. Aplica lo razonable y anota en el reporte lo que no apliques, con el porqué.
- **Commits atómicos sugeridos:**
  1. `chore(claude): CLAUDE.md, ajustes y gancho de commits`
  2. `chore(speckit): Spec Kit y constitución`
  3. `build: minSdk 30, versiones y splashscreen`
  4. `feat(designsystem): colores, tipografía, formas y espacios de Rola`
  5. `feat(marca): ícono adaptativo y splash`
  6. `test(designsystem): contraste y tokens de marca`
- Comprueba con `git log -8 --format=%B` que ningún mensaje tenga líneas de autoría.
- Haz push a `origin development`. **No** abras PR ni fusiones a `main`.

## Paso 9 · Reporte y detente
- Llena `ai/handoff/reporte.md` con el formato de ese archivo:
  - números reales de Gradle, rutas y capturas;
  - si existe el comando de errores de Spec Kit (PEND-06);
  - qué marcaste como **REQUIERE REVISIÓN HUMANA**;
  - cambios que propongas al grafo.
- Cambia el estado de este archivo a **hecha** y **detente**.

---

## Fuera de esta tarea
Navegación, Media3, Room, DataStore, pantallas reales, widget y LRCLIB.
