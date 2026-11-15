# Modelo de datos: Onboarding completo

## Preferencias del usuario (KV-DATASTORE)

Archivo DataStore Preferences `rola_ajustes`. En esta función solo existe una clave; los demás campos de KV-DATASTORE llegan con Ajustes.

| Clave | Tipo | Valor por defecto | Escribe | Lee |
|---|---|---|---|---|
| `onboarding_completed` | booleano | `false` (clave ausente) | «Empezar» u «Omitir» | `MainActivity` al arrancar |

**Transiciones**: `false` → `true` una sola vez. Nada la regresa a `false` (salvo borrar los datos de la app desde Ajustes del sistema).

**Errores**: si el archivo no se puede leer (`IOException`), se toma `false` y se vuelve a mostrar la bienvenida. Otros errores se propagan (no se ocultan).

## Estado de la bienvenida (solo interfaz)

| Dato | Dónde vive | Sobrevive a |
|---|---|---|
| Página actual (0–2) | `PagerState` (`rememberPagerState`, guardable) | rotación y segundo plano; no a cerrar la app |
| ¿Completada? | `PreferencesRepository.onboardingCompleted` | todo |

## Permiso de música

Lo da el sistema (`READ_MEDIA_AUDIO` en 13+, `READ_EXTERNAL_STORAGE` en 11–12). Sin cambios: `MusicPermissionGate` lo revisa en cada `ON_RESUME`.
