# Checklist de calidad de la spec: Inicio y Buscar

**Propósito**: validar que la spec esté completa antes de planear
**Creada**: 2026-11-25
**Spec**: [spec.md](../spec.md)

## Calidad del contenido

- [x] Sin detalles de implementación (lenguajes, frameworks, APIs)
- [x] Centrada en el valor para el usuario
- [x] Escrita para personas no técnicas
- [x] Secciones obligatorias completas

## Completitud de requisitos

- [x] No quedan marcas [NEEDS CLARIFICATION]
- [x] Requisitos verificables y sin ambigüedad
- [x] Criterios de éxito medibles
- [x] Criterios de éxito sin tecnología
- [x] Escenarios de aceptación definidos
- [x] Casos límite identificados
- [x] Alcance acotado
- [x] Dependencias y supuestos identificados

## Lista para planear

- [x] Cada requisito funcional tiene criterio de aceptación
- [x] Las historias cubren los flujos principales
- [x] La función cumple los resultados medibles
- [x] No se filtran detalles de implementación

## Notas

- FR-040 nombra el NavHost porque lo exige DEC-28 (`biblioteca_fuera_del_navhost`); es una restricción del grafo.
- El temporizador de sueño se preguntó a Brian (Paso B2) y no tuvo respuesta; FR-029 no lo guarda y el choque entre DEC-31 y `sleep_timer_end` de KV-DATASTORE va al reporte.
- Los supuestos que salen de diferencias entre Figma y el grafo (fecha, mosaico Favoritas, grupo Listas) van al reporte.
