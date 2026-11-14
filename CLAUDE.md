# Rola

Reproductor de música local para Android (Kotlin, Jetpack Compose, Material 3, Media3, Room, DataStore).
Sin conexión primero; la única red es LRCLIB y solo para letras. minSdk 30, MVVM, la reproducción vive en un `MediaSessionService`.

## Dónde está la verdad
| Qué | Dónde |
|---|---|
| Decisiones, invariantes, pantallas | `ai/context/knowledge-graph.json` (busca con Grep por id: `DEC-xx`, `INV-xx`, `SCR-xx`; no lo leas entero) |
| Diseño visual | Figma, archivo `SgqjjZGWGQKXuJVtv5VmNf`, página «Identidad · Propuestas» (cada pantalla trae `figma_nodo` en el grafo) |
| Marca (tokens, fuentes, logo) | `docs/marca/README.md` |
| Tareas y reportes | `ai/handoff/` |

Si el código, Figma y el grafo chocan: manda el grafo para decisiones y Figma para lo visual (INV-08). No se adivina: se anota en el reporte.
Lo «propuesto» o «pendiente» del grafo no se asume (INV-09). Este agente **no edita el grafo**: propone cambios en el reporte.

## Ciclo de trabajo (`ai/handoff/README.md`)
1. La sesión de diseño escribe `ai/handoff/siguiente.md` (estado `lista`).
2. Se lee completo y se ejecuta paso por paso, sin hacer nada que no esté ahí. El estado pasa a `en curso`.
3. Al terminar se escribe `ai/handoff/reporte.md` (formato en ese archivo), capturas en `ai/handoff/capturas/` (`tNN-descripcion.png`), el estado pasa a `hecha` o `esperando …`, y **se detiene**.
4. Lo marcado **REQUIERE REVISIÓN HUMANA** se anota también en `ai/handoff/revisiones-pendientes.md`.

## Invariantes (obligatorios)
- **INV-01** La única red es LRCLIB y solo con el interruptor encendido. Sin analítica, publicidad ni rastreo.
- **INV-02** Nunca se borra un archivo del usuario sin el aviso del sistema (`MediaStore.createDeleteRequest`).
- **INV-03** Ningún secreto en el repositorio (llaves, `keystore.properties`, `local.properties`).
- **INV-04** Colores, tipografía, formas y espacios salen de los tokens del tema; ningún hex en pantallas o componentes.
- **INV-05** Toda operación del usuario tiene retroalimentación visible (DEC-09).
- **INV-06** Accesibilidad: objetivos ≥ 48 dp, descripción en botones de solo ícono, contraste ≥ 4.5:1, respetar «reducir movimiento» y el tamaño de letra del sistema.
- **INV-07** Commits atómicos, en español, sin líneas de autoría de IA.
- **INV-08** Las decisiones mandan desde el grafo; lo visual, desde Figma.
- **INV-09** Lo de `fuera_de_alcance` no se implementa sin aprobación; lo propuesto/pendiente no se asume.
- **INV-10** Las respuestas de LRCLIB se validan y nunca bloquean la reproducción.
- **INV-11** La reproducción solo se detiene con pausa/detener, nunca por una acción de interfaz.

## Gradle (Windows, desde la raíz)
```
.\gradlew :app:assembleDebug
.\gradlew :app:testDebugUnitTest
.\gradlew :app:lintDebug
```

## Convenciones
- Kotlin y Compose. Un solo módulo `:app`.
- Paquetes (DEC-20): `com.luis.rola.core.{designsystem,media,data,lyrics}` y `com.luis.rola.feature.{onboarding,home,library,playlist,search,player,lyrics,settings,widget}`.
- Textos de interfaz en `strings.xml`, en español.
- Nada de colores hex fuera de `core/designsystem` (INV-04). Estilo plano, bento y minimalista (DEC-27).
- No agregues dependencias que la tarea no pida; si hace falta una, justifícala en el reporte y espera.
- No leas, abras, imprimas ni copies `keystore.properties`, `*.jks`, `*.keystore`, `*.p12`, `.env*` ni `local.properties`.

## Git
- Rama `development`. Nunca push a `main` ni PR salvo que la tarea lo pida.
- Commits atómicos con el formato `tipo(área): descripción`, en español, **sin autoría de IA**: nada de `Co-Authored-By`, `Claude-Session` ni «Generated with».
- Antes del push revisa con `git log` que ningún mensaje tenga esas líneas.
- Al clonar, activa el gancho de respaldo (la configuración es local y no viaja con el repositorio):
  ```
  git config core.hooksPath .githooks
  ```

## Spec Kit y Ponytail
| Tipo de tarea | Spec Kit | Ponytail |
|---|---|---|
| Función nueva | `/speckit.specify` → `clarify` → `plan` → `tasks` → `analyze` → `implement` | Activo (`full`), apagado mientras se escribe la spec |
| Corrección de error | Comando de errores si existe (PEND-06); si no, especificación corta del fallo | Activo |
| Configuración o fundaciones | Solo `/speckit.constitution` si cambian los principios | Activo |
| Diseño en Figma o textos | No | Apagado |
| Cierre de cada tarea | — | `/ponytail:ponytail-review` sobre el diff, antes de cada commit |

Las especificaciones citan ids del grafo en lugar de copiar decisiones.
