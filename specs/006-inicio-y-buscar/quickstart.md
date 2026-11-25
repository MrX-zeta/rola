# Validación rápida: Inicio y Buscar

## Requisitos

- Emulador Pixel 7 Pro con audios en `/sdcard/Music`, alguno de más de 60 s (para probar el minuto y los 30 s) y alguno de menos de 30 s.
- `ANDROID_HOME` apuntando al SDK.

## Comprobaciones automáticas

```
.\gradlew :app:testDebugUnitTest          # PlaybackStoreTest, PlayCounterTest, HistoryTest, SearchTest, GreetingTest
.\gradlew :app:connectedDebugAndroidTest  # MigrationTest, RestorePlaybackTest, HomeScreenTest, SearchScreenTest y el resto
.\gradlew :app:assembleDebug :app:assemblePerfil :app:lintDebug
```

## Recorrido manual

1. **Restaurar** (FR-020 a FR-024): reproducir un álbum, reordenar la cola, llevar una canción a la mitad, deslizar la app fuera de recientes; reabrir. Repetir con «Forzar detención» desde Ajustes del sistema. Esperado: misma canción, minuto (±5 s), cola y origen; en pausa.
2. **Aleatorio** (FR-020): con aleatorio y repetir encendidos, cerrar y reabrir: mismos modos y orden; apagar aleatorio vuelve al orden del álbum.
3. **Canción borrada** (FR-024): con la app cerrada, borrar con adb una canción de la cola; reabrir: no está y no hay aviso.
4. **Historial** (FR-010): escuchar 30 s de una canción larga y una corta completa; saltar otra antes de 30 s. En Inicio aparecen solo las dos primeras.
5. **Inicio** (FR-001 a FR-009): abre en Inicio; saludo y fecha; tocar carrusel, «Tu más escuchada», Aleatorio y Favoritas («atrás» regresa a Inicio).
6. **Primera vez** (FR-007): con datos borrados; «Ir a Biblioteca».
7. **Sin canciones** (FR-015 a FR-017): sin audios en el teléfono; copiar uno y «Escanear de nuevo»: progreso, aviso y aparece.
8. **Buscar** (FR-030 a FR-039): teclado al entrar; «mar» con resaltado y grupos; «cancion» encuentra «Canción»; sin resultados; abrir un álbum y volver con «atrás» a los mismos resultados; ⋮.
9. **Pestañas** (FR-040): abrir un detalle en cada pestaña, ir y volver entre las tres; tocar la activa vuelve a su raíz.
10. **Actualizar** (FR-046): instalar la versión anterior con favoritas y una lista; instalar esta encima: siguen.
11. **2 000 canciones** (SC-003, SC-005): reproducir todo, arrastrar en la cola, buscar; juzgar fluidez en `perfil` (en el Redmi si está).
12. **Reducir movimiento**: sin cascada en Inicio ni Buscar.
