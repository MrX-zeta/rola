# Revisiones pendientes

Lo que necesita una decisión del dueño o revisión humana. Cuando se resuelva, se marca con la fecha y se refleja en el grafo.

---

## Decisiones del dueño

### R1 · Interruptor de notificaciones (PEND-01) — **resuelta 2026-11-14**: «Otros avisos»; la notificación del reproductor siempre se queda.
- Para seguir sonando con la app cerrada, Android exige que el reproductor muestre su notificación. Si se oculta, la música se detiene al salir.
- **Propuesta:** el interruptor apaga los *otros* avisos (escaneo terminado, letra descargada); la notificación multimedia se queda.
- Bloquea: la tarea de Ajustes. No bloquea la tarea 01.

### R2 · Neutros cálidos (PEND-03) — **resuelta 2026-11-14**: aprobados.
- Grises con un toque cálido (`#10100E`, `#1B1A17`) para alejar la sensación «cripto/Spotify». Comparación en Figma, página «Rola · Kit de marca».
- La tarea 01 ya los usa; si se rechazan, solo cambian los hex en `Color.kt`.

### R3 · Inyección de dependencias (PEND-02) — **propuesta**
- Manual mientras la app sea pequeña; Hilt cuando crezca. Se decide en la tarea 02.

### R4 · Letras dentro del audio (PEND-04), artículos al ordenar (PEND-05), duración mínima al escanear (PEND-07) — **propuestas**
- Ver el grafo. No bloquean nada todavía.

### R5 · Alcance del color de la portada (PEND-08) — **resuelta 2026-11-14**: opción A (solo reproductor, cola, letras y mini reproductor).

## Revisión humana de código
### Tarea 01 (2026-11-14)
- `.claude/settings.json`: atribución vacía y lecturas denegadas de secretos (no cubre `local.properties`).
- `.githooks/commit-msg` y `.gitattributes`: respaldo de INV-07; hay que correr `git config core.hooksPath .githooks` al clonar.
- Spec Kit sin instalar (`uv`, `specify`): bloquea el Paso 2 y PEND-06.
- Íconos temáticos (capa monocroma): verlos en el teléfono; el emulador no los mostró.

**Revisada por la sesión de diseño (2026-11-14):** configuración aprobada. Se agrega `local.properties` a las lecturas denegadas en la tarea 01b. Spec Kit pasa a la 01b. Íconos temáticos: pendiente que Brian los vea en su teléfono.

### Tarea 06 (2026-11-19)
- Fluidez de Biblioteca con 2 000 canciones y 200 álbumes (SC-004): el emulador dibuja por software; falta verla en el Redmi con `perfil`.
- Artistas agrupados por artista del álbum: una canción con invitado no crea su propio artista (reporte, decisión 1).

### Tarea 07 (2026-11-25)
- **Resuelta 2026-11-26:** se desactiva la copia (`allowBackup=false`) en la tarea 08. Antes: `allowBackup` lleva `rola.db` y la cola a otro teléfono, donde los ids del MediaStore son otros (reporte, riesgos).
- Temporizador de sueño: DEC-31 y KV-DATASTORE se contradicen; hoy no se guarda.
- Deslizar la app fuera de recientes y fluidez con 2 000 canciones: verlas en el Redmi.

**Revisada por la sesión de diseño (2026-11-26):** decisiones 1–10 aprobadas; `play_order` aprobado; el temporizador de sueño no se guarda; falta que Brian deslice la tarea en el Redmi y pruebe «Escanear de nuevo» con un archivo nuevo.

### Tarea 08 (2026-11-26)
- **REQUIERE REVISIÓN HUMANA:** copia de seguridad apagada: `allowBackup="false"`, se borró `backup_rules.xml` y `data_extraction_rules.xml` excluye todo en la nube y en el traspaso entre teléfonos (en Android 12+ `allowBackup` solo apaga la nube). Commit `feaa291`.
- Símbolo de Rola en tema claro: la «r» del drawable es blanca fija y casi no se ve sobre el fondo claro (reporte, diferencias en claro).
