# AntiPanel Frontend - Documentacion de Diseno

## Indice

1. [Arquitectura CSS y Comunicacion Visual](#1-arquitectura-css-y-comunicacion-visual)
2. HTML Semantico y Estructura (Fase 2)
3. Sistema de Componentes UI (Fase 3)

---

## 1. Arquitectura CSS y Comunicacion Visual

Esta seccion documenta los fundamentos del sistema de diseno de AntiPanel, incluyendo la metodologia CSS, los design tokens y las herramientas SCSS.

### 1.1 Principios de Comunicacion Visual

El diseno de AntiPanel sigue una filosofia minimalista con enfasis en:

**Jerarquia Visual**
- Uso de tamanos tipograficos con escala definida (128px - 10px)
- Contraste alto entre texto y fondo (#FAFAFA sobre #0A0A0A)
- Espaciado consistente basado en sistema de 8px

**Paleta de Colores (Dark Mode)**

| Token | Valor | Uso |
|-------|-------|-----|
| `--color-background` | `#0A0A0A` | Fondo principal |
| `--color-text` | `#FAFAFA` | Texto principal |
| `--color-high-contrast` | `#FFFFFF` | Texto alto contraste |
| `--color-foreground` | `#A1A1A1` | Texto secundario |
| `--color-secondary` | `#666666` | Borders, texto terciario |
| `--color-information` | `#393939` | Borders, separadores |
| `--color-tiny-info` | `#1C1C1C` | Fondos secundarios |
| `--color-success` | `#00DC33` | Exito, CTA principal |
| `--color-error` | `#FF4444` | Errores |
| `--color-status-yellow` | `#F0B100` | Pendiente, warning |
| `--color-stats-blue` | `#00A5FF` | Estadisticas |

**Tipografia**

- **Fuente Primaria**: Montserrat (headings, UI elements)
- **Fuente Secundaria**: IBM Plex Mono (code, data, inputs)

| Tamano | Pixeles | Uso |
|--------|---------|-----|
| Title | 128px | Titulos hero |
| H1 | 96px | Secciones principales |
| H2 | 64px | Subsecciones |
| H3 | 48px | Titulos componentes |
| H4 | 32px | Subtitulos |
| H5 | 24px | Labels, buttons |
| H6 | 20px | Small headings |
| Body | 16px | Texto normal |
| Caption | 14px | Texto pequeno |
| Small | 12px | Muy pequeno |
| Tiny | 10px | Minimo |

### 1.2 Metodologia CSS: ITCSS + BEM + Angular Emulated

AntiPanel utiliza una metodologia hibrida que combina lo mejor de tres enfoques:

**ITCSS (Inverted Triangle CSS)**

Arquitectura de capas ordenadas por especificidad creciente:

```
src/styles/
├── 00-settings/     # Variables SCSS y CSS Custom Properties
├── 01-tools/        # Mixins y funciones
├── 02-generic/      # Reset y normalize
├── 03-elements/     # Estilos base HTML (sin clases)
└── 04-layout/       # Grid system y layouts
```

**BEM (Block Element Modifier)**

Convencion de nomenclatura para clases CSS:

```scss
// Block
.card { }

// Element
.card__title { }
.card__content { }
.card__footer { }

// Modifier
.card--featured { }
.card--compact { }
```

**Angular Emulated ViewEncapsulation**

Los componentes Angular usan encapsulacion emulada por defecto:

```typescript
@Component({
  selector: 'app-button',
  templateUrl: './button.html',
  styleUrl: './button.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
```

### 1.3 Organizacion de Archivos

**Estructura ITCSS**

```
src/styles/
├── 00-settings/
│   ├── _index.scss           # Forward de modulos
│   ├── _variables.scss       # Variables SCSS (breakpoints, spacing)
│   └── _css-variables.scss   # CSS Custom Properties (:root)
├── 01-tools/
│   ├── _index.scss           # Forward de mixins
│   └── _mixins.scss          # Mixins reutilizables
├── 02-generic/
│   ├── _index.scss           # Imports de reset
│   └── _reset.scss           # CSS Reset moderno
├── 03-elements/
│   ├── _index.scss           # Imports de elementos
│   └── _base.scss            # Estilos base HTML
├── 04-layout/
│   ├── _index.scss           # Imports de layout
│   └── _layout.scss          # Grid system
└── styles.scss               # Entry point principal
```

**Entry Point (styles.scss)**

```scss
// ==========================================================================
// MAIN STYLESHEET - ITCSS ARCHITECTURE
// ==========================================================================

// SETTINGS - Variables and configuration
@use 'styles/00-settings' as settings;

// TOOLS - Mixins and functions (no CSS output)
@use 'styles/01-tools' as tools;

// GENERIC - Reset and normalize
@use 'styles/02-generic';

// ELEMENTS - Base HTML element styles
@use 'styles/03-elements';

// LAYOUT - Grid system and layout utilities
@use 'styles/04-layout';
```

### 1.4 Sistema de Design Tokens

Los design tokens se dividen en dos tipos:

**Variables SCSS** (`_variables.scss`)

Usadas para calculos y mixins (no accesibles en runtime):

```scss
// Breakpoints
$breakpoints: (
  'sm': 640px,
  'md': 768px,
  'lg': 1024px,
  'xl': 1280px,
  '2xl': 1440px
);

// Spacing scale (base-8)
$spacing-1: 0.25rem;   // 4px
$spacing-2: 0.5rem;    // 8px
$spacing-4: 1rem;      // 16px
$spacing-8: 2rem;      // 32px
// ... etc

// Typography sizes
$font-sizes: (
  'title': 8rem,       // 128px
  'h1': 6rem,          // 96px
  'h2': 4rem,          // 64px
  // ... etc
);
```

**CSS Custom Properties** (`_css-variables.scss`)

Accesibles en runtime para theming dinamico:

```scss
:root {
  // Colors
  --color-background: #0a0a0a;
  --color-text: #fafafa;
  --color-success: #00dc33;

  // Typography
  --font-primary: 'Montserrat', sans-serif;
  --font-secondary: 'IBM Plex Mono', monospace;
  --font-size-body: 1rem;

  // Spacing (generated from scale)
  --spacing-1: 0.25rem;
  --spacing-2: 0.5rem;
  --spacing-4: 1rem;

  // Transitions
  --transition-fast: 150ms ease-in-out;
  --transition-base: 300ms ease-in-out;

  // Border radius
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 16px;
}
```

### 1.5 Mixins y Funciones

El archivo `_mixins.scss` proporciona herramientas reutilizables:

**Responsive Breakpoints**

```scss
// Mobile-first (min-width)
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media screen and (min-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  }
}

// Desktop-first (max-width)
@mixin respond-below($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media screen and (max-width: (map-get($breakpoints, $breakpoint) - 1px)) {
      @content;
    }
  }
}

// Uso:
.component {
  padding: var(--spacing-4);

  @include respond-to('md') {
    padding: var(--spacing-8);
  }
}
```

**Flexbox Utilities**

```scss
@mixin flex-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

@mixin flex-between {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@mixin flex-column {
  display: flex;
  flex-direction: column;
}
```

**Typography**

```scss
@mixin typography($size, $weight: 'regular', $family: 'primary') {
  @if $family == 'primary' {
    font-family: var(--font-primary);
  } @else {
    font-family: var(--font-secondary);
  }

  font-size: var(--font-size-#{$size});
  font-weight: var(--font-weight-#{$weight});
}
```

**Accessibility**

```scss
@mixin focus-visible($color: var(--focus-ring-color)) {
  &:focus {
    outline: none;
  }

  &:focus-visible {
    outline: var(--focus-ring-width) solid $color;
    outline-offset: var(--focus-ring-offset);
  }
}

@mixin visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
```

**Component Patterns**

```scss
@mixin button-base {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-2);
  padding: var(--spacing-3) var(--spacing-6);
  font-family: var(--font-primary);
  font-weight: var(--font-weight-semibold);
  text-transform: uppercase;
  cursor: pointer;
  border: none;
  border-radius: var(--radius-lg);
  transition: var(--transition-colors);

  @include focus-visible;
}

@mixin card-base {
  background-color: var(--color-background);
  border: var(--border-width-heavy) solid var(--color-information);
  border-radius: var(--radius-xl);
  padding: var(--spacing-8);
  transition: var(--transition-colors);
}
```

### 1.6 ViewEncapsulation en Angular

Angular ofrece tres modos de encapsulacion de estilos:

**Emulated (Default - Usado en AntiPanel)**

```typescript
@Component({
  encapsulation: ViewEncapsulation.Emulated // Default, no need to specify
})
```

- Emula Shadow DOM usando atributos unicos (`_ngcontent-xxx`)
- Los estilos del componente no afectan a otros componentes
- Permite usar variables CSS globales

**None**

```typescript
@Component({
  encapsulation: ViewEncapsulation.None
})
```

- Los estilos son globales
- Util para estilos que deben afectar a toda la aplicacion
- Usar con cuidado para evitar conflictos

**ShadowDom**

```typescript
@Component({
  encapsulation: ViewEncapsulation.ShadowDom
})
```

- Usa Shadow DOM nativo del navegador
- Aislamiento completo de estilos
- Las variables CSS funcionan a traves del shadow boundary

**Estrategia de AntiPanel**

1. **Estilos globales** (ITCSS) en `src/styles/`
   - Reset, elementos base, layout utilities
   - Accesibles por toda la aplicacion

2. **Estilos de componente** (Emulated) en cada componente
   - Estilos especificos del componente
   - Usan BEM para nomenclatura
   - Acceden a CSS Custom Properties globales

```scss
// src/app/components/button/button.scss
.button {
  @include button-base;

  &--primary {
    background-color: var(--color-success);
    color: var(--color-background);
  }

  &--secondary {
    background-color: transparent;
    border: var(--border-width-medium) solid var(--color-text);
    color: var(--color-text);
  }

  &:hover {
    transform: translateY(-2px);
  }
}
```

---

## Proximas Secciones

- **Seccion 2**: HTML Semantico y Estructura (Fase 2)
- **Seccion 3**: Sistema de Componentes UI (Fase 3)
