# Revisiones pendientes

Lo que necesita una decisión del dueño o revisión humana. Cuando se resuelva, se marca con la fecha y se refleja en el grafo.

---

## Decisiones del dueño

### R1 · Interruptor de notificaciones (PEND-01) — **abierta**
- Para seguir sonando con la app cerrada, Android exige que el reproductor muestre su notificación. Si se oculta, la música se detiene al salir.
- **Propuesta:** el interruptor apaga los *otros* avisos (escaneo terminado, letra descargada); la notificación multimedia se queda.
- Bloquea: la tarea de Ajustes. No bloquea la tarea 01.

### R2 · Neutros cálidos (PEND-03) — **propuesta**
- Grises con un toque cálido (`#10100E`, `#1B1A17`) para alejar la sensación «cripto/Spotify». Comparación en Figma, página «Rola · Kit de marca».
- La tarea 01 ya los usa; si se rechazan, solo cambian los hex en `Color.kt`.

### R3 · Inyección de dependencias (PEND-02) — **propuesta**
- Manual mientras la app sea pequeña; Hilt cuando crezca. Se decide en la tarea 02.

### R4 · Letras dentro del audio (PEND-04), artículos al ordenar (PEND-05), duración mínima al escanear (PEND-07) — **propuestas**
- Ver el grafo. No bloquean nada todavía.

## Revisión humana de código
### Tarea 01 (2026-11-14)
- `.claude/settings.json`: atribución vacía y lecturas denegadas de secretos (no cubre `local.properties`).
- `.githooks/commit-msg` y `.gitattributes`: respaldo de INV-07; hay que correr `git config core.hooksPath .githooks` al clonar.
- Spec Kit sin instalar (`uv`, `specify`): bloquea el Paso 2 y PEND-06.
- Íconos temáticos (capa monocroma): verlos en el teléfono; el emulador no los mostró.
