# Modelo de datos: Ajustes y legal

## DataStore `rola_ajustes` (KV-DATASTORE)

| Clave | Tipo | Por defecto | Notas |
|---|---|---|---|
| `theme` | texto `system` \| `dark` \| `light` | `dark` | Valor desconocido o ilegible → `dark`. |
| `last_scan_at` | entero largo (ms de época) | ausente | Ausente → «Aún no has escaneado». Se escribe al terminar cada escaneo (Ajustes o SCR-12). |

Claves existentes sin cambios: `onboarding_completed`, `sort_order`. Ninguna migración: las claves nuevas solo se agregan.

Si el archivo no se puede leer, se usan los valores por defecto; si no se puede escribir, el valor se aplica en pantalla y no se recuerda (FR-075), como ya hace `sort_order`.

## ThemeMode

`SYSTEM`, `DARK`, `LIGHT`. Resolución a oscuro/claro: `SYSTEM` → modo oscuro del teléfono; `DARK` → oscuro; `LIGHT` → claro.

## TimeAgo (calculado, no se guarda)

| Diferencia `now − then` | Tramo | Texto |
|---|---|---|
| < 60 s o negativa | `Moment` | «hace un momento» |
| < 60 min | `Minutes(n)` | «hace 1 minuto» / «hace N minutos» |
| < 24 h | `Hours(n)` | «hace 1 hora» / «hace N horas» |
| ≥ 24 h | `Days(n)` | «hace 1 día» / «hace N días» |

`n` se redondea hacia abajo. La fila antepone «Último escaneo: ».

## Estado del escaneo

`LibraryScanner.scanning`: `true` mientras corre un escaneo. Un segundo pedido mientras corre no inicia otro (devuelve `null`, sin aviso).

## Temporizador (sin cambios de datos)

Se lee de `PlayerState.timer` y `PlayerState.timerEndsAtMs`. No se guarda (KV-DATASTORE).

## LegalDoc (fijo en la app)

`TERMS`, `PRIVACY`, `LICENSES`; cada uno con título, «Última actualización» y una lista de secciones (encabezado, párrafo) en `strings.xml`.
