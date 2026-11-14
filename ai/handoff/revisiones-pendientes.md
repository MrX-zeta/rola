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
_(Claude Code agrega aquí lo marcado como «REQUIERE REVISIÓN HUMANA».)_
