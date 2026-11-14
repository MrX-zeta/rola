# Constitución de Rola

El grafo `ai/context/knowledge-graph.json` es la fuente de verdad. Esta constitución **cita** sus ids y no copia sus decisiones: ante cualquier duda se lee el nodo.

## Artículos

### I. El grafo manda (INV-08, INV-09)
Si una spec, plan o tarea contradice al grafo, manda el grafo. Lo visual sale de Figma. Lo «propuesto», «pendiente» o «fuera de alcance» no se implementa ni se asume: se pregunta.

### II. Sin conexión primero (DEC-01, INV-01, INV-10)
Rola funciona sin red. La única llamada permitida es LRCLIB, apagada por defecto y siempre validada. Cero analítica, publicidad y rastreo.

### III. Datos y secretos del usuario (INV-02, INV-03)
Nunca se borra un archivo del usuario sin el aviso del sistema. Ningún secreto entra al repositorio ni se lee desde Claude Code.

### IV. Tokens del tema (INV-04, DEC-27)
Colores, tipografía, formas y espacios salen de `core/designsystem`; nada de hex en pantallas. La app es plana y bento; el relieve solo donde DEC-27 lo permite.

### V. Retroalimentación visible (INV-05)
Toda operación del usuario muestra su estado: progreso, éxito o error.

### VI. Accesibilidad (INV-06)
Objetivos táctiles ≥ 48 dp, descripciones en botones de ícono, contraste ≥ 4.5:1, «reducir movimiento» y tamaño de letra del sistema respetados.

### VII. La reproducción no se interrumpe (INV-11)
Ninguna acción de interfaz distinta de pausa o detener corta la música.

### VIII. Arquitectura (DEC-20)
Un módulo `app`; paquetes `core/` y `feature/`; MVVM con flujo de datos en una sola dirección; reproducción en un `MediaSessionService`.

### IX. Pruebas (DEC-22)
Toda lógica nueva lleva pruebas (JUnit para lógica pura; Compose y Maestro según la tarea).

### X. Código mínimo (Ponytail)
Se escribe lo menos que resuelva el problema completo, sin recortar seguridad ni accesibilidad.

### XI. Forma de trabajo y español (DEC-21, INV-07)
Cada tarea viene de `ai/handoff/siguiente.md` y termina en `reporte.md`. Commits atómicos, con formato `tipo(área): descripción`, sin autoría de IA. Interfaz, commits y documentos, en español.

## Gobernanza
Esta constitución se enmienda solo cuando cambien los principios, con `/speckit-constitution`, aprobada por Brian. Los ids citados deben seguir existiendo en el grafo; si el grafo cambia, se revisa el artículo que lo cita.

**Versión**: 1.0.0 | **Ratificada**: 2026-11-14 | **Última enmienda**: 2026-11-14
