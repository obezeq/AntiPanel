# AntiPanel Frontend - Documentacion de Diseno

## Indice

1. [Arquitectura CSS y Comunicacion Visual](#1-arquitectura-css-y-comunicacion-visual)
2. [HTML Semantico y Estructura](#2-html-semantico-y-estructura)
3. [Sistema de Componentes UI](#3-sistema-de-componentes-ui)

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

## 2. HTML Semantico y Estructura

Esta seccion documenta el uso de HTML semantico y la estructura de componentes de layout en AntiPanel.

### 2.1 Elementos Semanticos Utilizados

AntiPanel utiliza elementos HTML semanticos para mejorar la accesibilidad y el SEO:

| Elemento | Uso | Componente |
|----------|-----|------------|
| `<header>` | Cabecera del sitio | `Header` |
| `<nav>` | Navegacion principal y secundaria | `Header`, `Footer`, `Sidebar` |
| `<main>` | Contenido principal de la pagina | `MainContent` |
| `<aside>` | Navegacion lateral (admin) | `Sidebar` |
| `<footer>` | Pie de pagina | `Footer` |
| `<article>` | Contenido independiente | Brand section en Footer |
| `<section>` | Secciones de contenido | Agrupaciones logicas |
| `<form>` | Formularios | `AuthForm` |
| `<fieldset>` | Agrupacion de campos | `FormInput`, `AuthForm` |
| `<legend>` | Titulo de fieldset | `AuthForm` |

### 2.2 Estructura de Componentes de Layout

**Header Component**

```
<header class="header">
  <a class="header__logo">Logo</a>
  <nav class="header__nav">
    <ul role="list">
      <li><a>Link</a></li>
    </ul>
  </nav>
  <section class="header__actions">
    <a class="header__access">ACCESS</a>
  </section>
</header>
```

Caracteristicas:
- Logo con enlace a home
- Navegacion principal con lista semantica
- Acciones de usuario (login/wallet/profile)
- Menu hamburguesa responsive
- Variantes: `home`, `dashboard`, `admin`

**Footer Component**

```
<footer class="footer">
  <section class="footer__content">
    <article class="footer__brand">
      <a class="footer__logo">Logo</a>
      <p class="footer__copyright">Copyright</p>
    </article>
    <nav class="footer__nav">
      <ul role="list">Links</ul>
    </nav>
    <button class="footer__back-to-top">Back to Top</button>
  </section>
</footer>
```

Caracteristicas:
- Marca y copyright
- Links de navegacion secundaria
- Boton "Back to Top" con scroll suave

**Main Content Component**

```
<main class="main-content main-content--padded">
  <ng-content />
</main>
```

Variantes:
- `default` - Max-width 1440px
- `narrow` - Max-width 960px (formularios, articulos)
- `wide` - Max-width 1600px (dashboards)
- `fluid` - Sin max-width

**Sidebar Component**

```
<aside class="sidebar" aria-label="Admin navigation">
  <header class="sidebar__header">
    <h2 class="sidebar__title">ADMIN</h2>
  </header>
  <nav class="sidebar__nav">
    <ul role="list">
      <li><a class="sidebar__link">Dashboard</a></li>
    </ul>
  </nav>
</aside>
```

Caracteristicas:
- Navegacion sticky en desktop
- Panel deslizante en mobile
- Indicador visual de ruta activa

### 2.3 Jerarquia de Headings

AntiPanel sigue una jerarquia de headings consistente:

```
h1 - Titulo principal de la pagina (uno por pagina)
  h2 - Secciones principales
    h3 - Subsecciones
      h4 - Elementos dentro de subsecciones
```

Ejemplo en una pagina de dashboard:

```html
<main>
  <h1>Dashboard</h1>              <!-- Titulo de pagina -->
  <section>
    <h2>Statistics</h2>           <!-- Seccion -->
    <article>
      <h3>Orders Today</h3>       <!-- Stat card -->
    </article>
  </section>
  <section>
    <h2>Recent Orders</h2>        <!-- Otra seccion -->
  </section>
</main>
```

### 2.4 Estructura de Formularios

**Form Input Component**

```
<fieldset class="form-input">
  <label for="email" class="form-input__label">
    Email
    <span class="form-input__required">*</span>
  </label>
  <input
    id="email"
    type="email"
    class="form-input__field"
    aria-describedby="email-error"
    aria-invalid="true"
  />
  <p id="email-error" class="form-input__error" role="alert">
    Error message
  </p>
</fieldset>
```

Caracteristicas de accesibilidad:
- Labels asociados via `for/id`
- `aria-describedby` para mensajes de error
- `aria-invalid` para estado de validacion
- `role="alert"` para anunciar errores
- Indicador visual de campo requerido

**Auth Form Component**

```
<form class="auth-form" novalidate>
  <fieldset class="auth-form__fieldset">
    <legend class="auth-form__legend">Welcome Back</legend>

    <section class="auth-form__fields">
      <app-form-input label="Email" />
      <app-form-input label="Password" />
    </section>

    <section class="auth-form__actions">
      <button type="submit">LOGIN</button>
      <p>Don't have an account? <a>Register</a></p>
    </section>
  </fieldset>
</form>
```

Modos:
- `login` - Email + Password
- `register` - Email + Password + Confirm Password

### 2.5 Patrones de Accesibilidad

**Focus Management**

Todos los elementos interactivos tienen estilos de focus visibles:

```scss
&:focus-visible {
  outline: var(--focus-ring-width) solid var(--focus-ring-color);
  outline-offset: var(--focus-ring-offset);
}
```

**Skip Link**

El reset CSS incluye un skip link para navegacion por teclado:

```html
<a href="#main-content" class="skip-link">
  Skip to main content
</a>
```

**ARIA Attributes**

- `aria-label` en navegacion y botones sin texto visible
- `aria-expanded` en menus desplegables
- `aria-controls` vinculando toggles con contenido
- `role="list"` en listas estilizadas

**Reduced Motion**

Respeto a preferencias de usuario:

```scss
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
```

---

## 3. Sistema de Componentes UI

Esta seccion documenta los componentes UI reutilizables implementados en AntiPanel.

### 3.1 Componentes Implementados

| Componente | Ubicacion | Descripcion |
|------------|-----------|-------------|
| `Button` | `shared/button/` | Boton con variantes primary, secondary, ghost, danger |
| `Alert` | `shared/alert/` | Notificaciones success, error, warning, info |
| `FormInput` | `shared/form-input/` | Input con label, validacion y estados |
| `FormTextarea` | `shared/form-textarea/` | Textarea con contador de caracteres |
| `FormSelect` | `shared/form-select/` | Select dropdown con opciones dinamicas |
| `ServiceCard` | `shared/service-card/` | Tarjeta de servicio con icono y contador |
| `StatsCard` | `shared/stats-card/` | Tarjeta de estadisticas con cambio porcentual |
| `Modal` | `shared/modal/` | Dialogo modal accesible con backdrop |
| `OrderInput` | `shared/order-input/` | Input de orden en lenguaje natural |
| `DashboardHeader` | `shared/dashboard-header/` | Header grande para dashboards |
| `UserOrderRow` | `shared/user-order-row/` | Fila de orden para listados |
| `Header` | `layout/header/` | Cabecera con navegacion responsive |
| `Footer` | `layout/footer/` | Pie de pagina con links |
| `MainContent` | `layout/main-content/` | Contenedor principal |
| `Sidebar` | `layout/sidebar/` | Barra lateral para admin |
| `AuthForm` | `shared/auth-form/` | Formulario login/registro |

### 3.2 Nomenclatura BEM

Todos los componentes siguen la convencion BEM:

```scss
// Block
.button { }

// Element
.button__content { }
.button__spinner { }

// Modifier
.button--primary { }
.button--secondary { }
.button--sm { }
.button--lg { }
.button--loading { }
```

**Ejemplos de BEM en componentes:**

```scss
// Service Card
.service-card { }
.service-card__icon { }
.service-card__name { }
.service-card__count { }
.service-card--interactive { }

// Form Input
.form-input { }
.form-input__label { }
.form-input__field { }
.form-input__error { }
.form-input--focused { }
.form-input--error { }
.form-input--disabled { }

// Alert
.alert { }
.alert__icon { }
.alert__content { }
.alert__title { }
.alert__message { }
.alert__dismiss { }
.alert--success { }
.alert--error { }
.alert--warning { }
.alert--info { }
```

### 3.3 Patrones de Componentes Angular 21

**Inputs y Outputs con Signals:**

```typescript
@Component({
  selector: 'app-button',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Button {
  // Inputs con signal API
  readonly variant = input<ButtonVariant>('primary');
  readonly size = input<ButtonSize>('md');
  readonly disabled = input<boolean>(false);
  readonly loading = input<boolean>(false);

  // Required input
  readonly label = input.required<string>();

  // Outputs
  readonly buttonClick = output<MouseEvent>();

  // Computed values
  protected readonly isDisabled = computed(() =>
    this.disabled() || this.loading()
  );
}
```

**Control Flow en Templates:**

```html
<!-- @if -->
@if (loading()) {
  <span class="spinner"></span>
} @else {
  <ng-content />
}

<!-- @for -->
@for (item of items(); track item.id) {
  <app-item [data]="item" />
}

<!-- @switch -->
@switch (variant()) {
  @case ('success') {
    <ng-icon name="matCheck" />
  }
  @case ('error') {
    <ng-icon name="matError" />
  }
  @default {
    <ng-icon name="matInfo" />
  }
}
```

**ControlValueAccessor para Forms:**

```typescript
@Component({
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormInput),
      multi: true
    }
  ]
})
export class FormInput implements ControlValueAccessor {
  protected readonly value = signal<string>('');

  writeValue(value: string): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }
  // ...
}
```

### 3.4 Sistema de Iconos (ngicons)

AntiPanel usa `@ng-icons` con Material Icons e Iconoir:

**Configuracion en app.config.ts:**

```typescript
import { provideIcons } from '@ng-icons/core';
import { matHome, matDashboard } from '@ng-icons/material-icons/baseline';
import { iconoirInstagram, iconoirTiktok } from '@ng-icons/iconoir';

export const appConfig: ApplicationConfig = {
  providers: [
    provideIcons({
      matHome,
      matDashboard,
      iconoirInstagram,
      iconoirTiktok
    })
  ]
};
```

**Uso en templates:**

```html
<ng-icon name="matHome" size="24" />
<ng-icon name="iconoirInstagram" size="20" />
```

### 3.5 Style Guide

El proyecto incluye una pagina de Style Guide en `/style-guide` que muestra:

- Paleta de colores completa
- Escala tipografica
- Todos los variantes de botones
- Tipos de alertas
- Elementos de formulario
- Tarjetas y cards
- Iconos disponibles

Para acceder: `http://localhost:4200/style-guide`

### 3.6 Estructura de Archivos de Componentes

```
src/app/components/
├── layout/
│   ├── header/
│   │   ├── header.ts
│   │   ├── header.html
│   │   └── header.scss
│   ├── footer/
│   ├── main-content/
│   └── sidebar/
└── shared/
    ├── button/
    │   ├── button.ts
    │   ├── button.html
    │   └── button.scss
    ├── alert/
    ├── form-input/
    ├── form-textarea/
    ├── form-select/
    ├── service-card/
    ├── stats-card/
    ├── modal/
    ├── order-input/
    ├── dashboard-header/
    ├── user-order-row/
    └── auth-form/
```

---

## Resumen

AntiPanel Frontend implementa un sistema de diseno completo con:

1. **Arquitectura CSS ITCSS** con design tokens
2. **HTML Semantico** para accesibilidad
3. **Componentes Angular 21** con signals y control flow moderno
4. **BEM** para nomenclatura de clases
5. **ngicons** para iconografia
6. **Style Guide** para documentacion visual

Para mas informacion, consulta el codigo fuente de cada componente.
