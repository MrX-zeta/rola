# Validación rápida: Biblioteca completa

## Requisitos

- Emulador Pixel 7 Pro (o el Redmi) con audios de prueba en `/sdcard/Music` que tengan álbum, artista, artista del álbum, pista y año (al menos 3 álbumes, uno sin número de pista, uno de dos discos y canciones sin artista).
- `ANDROID_HOME` apuntando al SDK.

## Comprobaciones automáticas

```
.\gradlew :app:testDebugUnitTest          # LibraryGroupingTest, SongSortTest, PreferencesRepositoryTest
.\gradlew :app:connectedDebugAndroidTest  # LibraryScreenTest, AlbumArtistTest y el resto
.\gradlew :app:lintDebug
```

## Recorrido manual

1. Biblioteca abre en Canciones; los chips dicen Listas, Álbumes, Artistas, Canciones (FR-001).
2. Álbumes: cuadrícula con portada, nombre, artista y año; las celdas entran en cascada (FR-003, FR-006).
3. Abrir un álbum: canciones por número de pista, el disco 1 antes del 2, las que no tienen número al final; Reproducir → «Reproduciendo desde» dice el álbum (FR-007, FR-008).
4. Artistas: «Artista desconocido» aparece una vez; abrir un artista: carrusel de álbumes y canciones; tocar un álbum del carrusel abre su detalle (FR-009 a FR-012).
5. Canciones → botón de filtros → «Artista» y «Z→A»: la lista y el resumen cambian; cerrar y reabrir la app: se conserva (FR-013, FR-014).
6. Abecedario en Canciones, Álbumes y Artistas: tocar una letra salta a la primera coincidencia (FR-017).
7. «Reducir movimiento» encendido: sin cascada (SC-006).
8. Rendimiento (SC-004): cargar 2 000 audios con ~200 álbumes, instalar `perfil`, entrar a Biblioteca y cambiar entre chips sin trabas visibles.
