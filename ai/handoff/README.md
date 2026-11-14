# Handoff · cómo trabajamos

Dos participantes y una persona:
- **Sesión de diseño (consultor):** mantiene el grafo (`ai/context`), diseña en Figma, escribe cada tarea y revisa los reportes.
- **Claude Code:** implementa la tarea con pruebas, hace commits y reporta. No decide alcance.
- **Brian (dueño):** aprueba decisiones, resuelve lo marcado y prueba en su teléfono.

---

## Archivos

| Archivo | Quién escribe | Para qué |
|---|---|---|
| `siguiente.md` | Sesión de diseño | La tarea actual, completa. Claude Code solo la lee. |
| `reporte.md` | Claude Code | Qué hizo, comprobaciones reales y preguntas. Se reemplaza en cada tarea. |
| `revisiones-pendientes.md` | Ambos | Lo que necesita decisión o revisión humana. |
| `capturas/` | Claude Code | Capturas del emulador o teléfono: `tNN-descripcion.png`. |
| `../context/knowledge-graph.json` | Solo la sesión de diseño | Fuente de verdad. Claude Code propone cambios en el reporte. |
| `../context/estado-actual.md` | Sesión de diseño | Resumen corto de dónde vamos. |

## Ciclo de una tarea
1. La sesión de diseño escribe `siguiente.md` con estado **lista**.
2. Brian abre Claude Code en la raíz y escribe: *«Lee ai/handoff/siguiente.md y ejecútala.»*
3. Claude Code cambia el estado a **en curso**, trabaja, llena `reporte.md`, deja el estado en **hecha** (o **esperando …** si algo lo bloquea) y **se detiene**.
4. Brian comparte el reporte con la sesión de diseño, que revisa y escribe la siguiente tarea (la revisión de la anterior va al inicio de la nueva).

## Estados de `siguiente.md`
`lista` · `en curso` · `esperando revisión` · `esperando <algo>` · `hecha`

## Reglas para Claude Code
- Leer primero los nodos `invariante` del grafo y las decisiones que cite la tarea.
- Trabajar en la rama `development`. Nunca hacer push a `main` ni fusionar sin que la tarea lo pida.
- Commits atómicos, en español, sin líneas de autoría de IA (INV-07).
- Si algo de la tarea contradice el grafo o Figma: no adivinar; anotarlo en el reporte.
- Marcar con **«REQUIERE REVISIÓN HUMANA»** lo que toque permisos, borrado de archivos, red o configuración de herramientas.
- Números reales en el reporte: salen de Gradle o de la terminal, no se estiman.

## Cuándo usar cada herramienta

| Tipo de tarea | Spec Kit | Ponytail |
|---|---|---|
| Función nueva (pantalla, módulo) | `/speckit.specify` → `/speckit.clarify` (si hay dudas) → `/speckit.plan` → `/speckit.tasks` → `/speckit.analyze` → `/speckit.implement` | Activo (`full`) |
| Corrección de error | Comando de errores de Spec Kit si existe (PEND-06); si no, especificación corta del fallo | Activo |
| Configuración o fundaciones | Solo `/speckit.constitution` cuando cambien los principios | Activo |
| Diseño en Figma o textos | No | Apagado (`/ponytail off`) |
| Cierre de cada tarea | — | `/ponytail:ponytail-review` sobre el diff |
| Cada 4–5 tareas | — | `/ponytail:ponytail-audit` (la tarea lo indicará) |

Las especificaciones de Spec Kit citan ids del grafo (DEC-xx, SCR-xx, INV-xx) en lugar de copiar decisiones.

## Interfaz
Claude Code de escritorio (pestaña Code) es la opción por defecto. Los comandos `/plugin` y `/speckit.*` funcionan ahí. La terminal solo hace falta para instalar el CLI de Spec Kit (`uv`, `specify`).
