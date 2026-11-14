# Marca · Rola

Fuente de verdad visual: Figma, página **«Rola · Kit de marca»** (archivo `SgqjjZGWGQKXuJVtv5VmNf`).
Decisión en el grafo: **DEC-16** (identidad) y **PEND-03** (neutros cálidos).

---

## Idea
- En México una canción es una **rola**.
- El símbolo es una «r» que se apoya en un disco: juntas se leen «ro», como el brazo de un tocadiscos sobre el vinil.

## Archivos

| Archivo | Uso |
|---|---|
| `rola-simbolo-oscuro.svg` | Símbolo principal sobre fondo oscuro (r blanca, disco lima) |
| `rola-simbolo-claro.svg` | Sobre fondo claro (r y disco tinta, centro lima) |
| `rola-simbolo-negro.svg` / `rola-simbolo-blanco.svg` | Una tinta |
| `rola-logo-horizontal-oscuro.svg` / `-claro.svg` | Símbolo + palabra «rola» ya en trazos |
| `ic_launcher_background.svg` | Capa de fondo del ícono adaptativo (lima, 108 dp) |
| `ic_launcher_foreground.svg` | Capa frontal (108 dp; el dibujo cabe en la zona segura de 66 dp) |
| `ic_launcher_monochrome.svg` | Capa monocroma para íconos temáticos (Android 13+) |
| `rola-icono-512.svg` | Ícono completo para README, About de GitHub, etc. |

Las capas `ic_launcher_*` se convierten a **Vector Drawable** (`res/drawable/`), no a PNG.

## Reglas
- **Espacio libre** alrededor del logo: el radio del disco.
- **Tamaño mínimo:** 16 px el símbolo; 72 px de ancho el horizontal.
- **No:** rotar el símbolo, separar la «r» del disco, cambiar el lima por otro verde, poner el disco lima sobre fondo claro (se pierde: usar `rola-simbolo-claro.svg`).
- El lima **nunca** es texto sobre fondo claro (1.1:1). Para texto en tema claro: `#3D5200`.

## Colores (neutros cálidos, PEND-03)

| Rol (Material 3) | Oscuro (base) | Claro |
|---|---|---|
| background | `#10100E` | `#F6F6F4` |
| surface | `#1B1A17` | `#FFFFFF` |
| surfaceContainer | `#262521` | `#ECEBE6` |
| outline | `#34322D` | `#D9D7D0` |
| onSurface | `#FAF8F2` | `#10100E` |
| onSurfaceVariant | `#A9A69C` | `#5C5A53` |
| primary | `#C7FF2E` | `#3D5200` |
| onPrimary | `#10100E` | `#FFFFFF` |
| primaryContainer | `#27310C` | `#C7FF2E` |
| onPrimaryContainer | `#C7FF2E` | `#10100E` |

Contraste comprobado (texto ≥ 4.5:1): onSurface/background 17.9 · onSurfaceVariant/surface 7.1 (oscuro) y 6.4 (claro) · primary/background 16.1 (oscuro) y 8.1 (claro).

**Color dinámico:** la mini tarjeta y «Reproduciendo» toman el tono de la carátula (Palette). El acento de marca queda en navegación, botones principales y el mosaico destacado. No se usa el color del fondo de pantalla de Android (Material You).

## Tipografía (todas con licencia OFL)
- **Space Grotesk Bold**: títulos y la palabra del logo.
- **Geist** (Regular, Medium, SemiBold): interfaz y listas.
- **Geist Mono** (Medium): tiempos (`2:47`), números del mismo ancho.
- Sin japonés/coreano/chino: esos títulos caen a la fuente del sistema.

## Splash
- API de Splash Screen del sistema (Android 12+), sin pantalla propia.
- Fondo del tema (`#10100E` / `#F6F6F4`), ícono = capa frontal sobre círculo lima de 160 dp.
