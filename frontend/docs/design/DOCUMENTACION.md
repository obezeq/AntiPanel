# AntiPanel Frontend - Documentacion de Diseno

## Indice

1. [Arquitectura CSS y Comunicacion Visual](#1-arquitectura-css-y-comunicacion-visual)
2. [HTML Semantico y Estructura](#2-html-semantico-y-estructura)
3. [Sistema de Componentes UI](#3-sistema-de-componentes-ui)
4. [Estrategia Responsive](#4-estrategia-responsive)
5. [Optimizacion Multimedia](#5-optimizacion-multimedia)
6. [Sistema de Temas](#6-sistema-de-temas)
7. [Informe de Accesibilidad](#7-informe-de-accesibilidad)

---

## 1. Arquitectura CSS y Comunicacion Visual

Esta seccion documenta los fundamentos del sistema de diseno de AntiPanel, incluyendo la metodologia CSS, los design tokens y las herramientas SCSS.

### 1.1 Principios de Comunicacion Visual

El diseno de AntiPanel sigue una filosofia minimalista aplicando los 5 principios fundamentales de comunicacion visual:

#### Jerarquia Visual

La jerarquia visual guia la atencion del usuario a traves de la interfaz mediante:
- Uso de tamanos tipograficos con escala definida (128px - 10px)
- Contraste alto entre texto y fondo (#FAFAFA sobre #0A0A0A)
- Espaciado consistente basado en sistema de 8px

<!-- SCREENSHOT: Captura del Dashboard mostrando jerarquia de titulos (h1 grande, h2 mediano, texto body) -->
> **Placeholder Screenshot:** Dashboard con jerarquia tipografica visible (titulo 96px, subtitulo 24px, texto 16px)

#### Contraste

El contraste asegura legibilidad y distincion visual:
- Texto principal (#FAFAFA) sobre fondo oscuro (#0A0A0A) - Ratio 19.4:1
- Colores semanticos distinguibles: verde exito, rojo error, amarillo warning
- Bordes sutiles (#393939) para separar elementos

<!-- SCREENSHOT: Captura de alertas o botones mostrando contraste de colores -->
> **Placeholder Screenshot:** Componentes Alert mostrando los 4 tipos (success, error, warning, info) con alto contraste

#### Alineacion

La alineacion crea orden y conexion visual:
- Grid de 8px como base para todo el espaciado
- Elementos alineados consistentemente al grid
- Margenes y paddings siguiendo la escala de espaciado

<!-- SCREENSHOT: Captura del Style Guide mostrando grid y alineacion de componentes -->
> **Placeholder Screenshot:** Vista del Style Guide con componentes alineados al grid de 8px

#### Proximidad

La proximidad agrupa elementos relacionados:
- Campos de formulario con sus labels cercanos
- Botones de accion agrupados
- Secciones separadas por espaciado mayor

<!-- SCREENSHOT: Captura del formulario de login mostrando agrupacion de campos -->
> **Placeholder Screenshot:** Formulario Auth mostrando proximidad entre label e input

#### Repeticion

La repeticion crea consistencia y reconocimiento:
- Mismos colores semanticos en toda la app
- Tipografia consistente (Montserrat headings, IBM Plex Mono datos)
- Patrones de componentes repetidos (cards, buttons, inputs)

<!-- SCREENSHOT: Captura de multiples cards o componentes mostrando consistencia -->
> **Placeholder Screenshot:** Grid de Service Cards mostrando repeticion del patron visual

---

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

## 4. Estrategia Responsive

Esta seccion documenta la estrategia de diseno responsive de AntiPanel, basada en un enfoque mobile-first con breakpoints definidos.

### 4.1 Sistema de Breakpoints

AntiPanel utiliza un sistema de 5 breakpoints basado en anchos de dispositivo comunes:

| Breakpoint | Ancho | Dispositivos Objetivo |
|------------|-------|----------------------|
| `sm` | 640px | Moviles en landscape |
| `md` | 768px | Tablets en portrait |
| `lg` | 1024px | Tablets en landscape / Laptops pequenos |
| `xl` | 1280px | Laptops / Desktops |
| `2xl` | 1440px | Desktops grandes (referencia de diseno) |

**Variables SCSS:**

```scss
$breakpoint-sm: 640px;
$breakpoint-md: 768px;
$breakpoint-lg: 1024px;
$breakpoint-xl: 1280px;
$breakpoint-2xl: 1440px;

$breakpoints: (
  'sm': $breakpoint-sm,
  'md': $breakpoint-md,
  'lg': $breakpoint-lg,
  'xl': $breakpoint-xl,
  '2xl': $breakpoint-2xl
);
```

### 4.2 Enfoque Mobile-First

Todos los estilos base estan disenados para moviles, y los estilos para pantallas mas grandes se anaden mediante media queries `min-width`:

```scss
.component {
  // Estilos base (mobile)
  padding: var(--spacing-4);
  font-size: var(--font-size-body);

  // Tablet
  @include respond-to('md') {
    padding: var(--spacing-6);
  }

  // Desktop
  @include respond-to('lg') {
    padding: var(--spacing-8);
    font-size: var(--font-size-h6);
  }
}
```

### 4.3 Mixins Responsive

**respond-to (Mobile-first):**

```scss
@mixin respond-to($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media screen and (min-width: map-get($breakpoints, $breakpoint)) {
      @content;
    }
  } @else {
    @warn "Breakpoint '#{$breakpoint}' not found in $breakpoints map.";
  }
}
```

**respond-below (Desktop-first):**

```scss
@mixin respond-below($breakpoint) {
  @if map-has-key($breakpoints, $breakpoint) {
    @media screen and (max-width: (map-get($breakpoints, $breakpoint) - 1px)) {
      @content;
    }
  }
}
```

### 4.4 Componentes Responsive

**Header:**
- Mobile: Menu hamburguesa con panel lateral deslizante
- Desktop: Navegacion horizontal con enlaces visibles

```scss
.header__nav {
  // Mobile: oculto por defecto
  position: fixed;
  transform: translateX(100%);

  @include respond-to('lg') {
    // Desktop: visible y horizontal
    position: static;
    transform: none;
    flex-direction: row;
  }
}
```

**Grid de Servicios:**

```scss
.grid--services {
  grid-template-columns: repeat(2, 1fr);  // Mobile: 2 columnas

  @include respond-to('sm') {
    grid-template-columns: repeat(3, 1fr); // Tablet: 3 columnas
  }

  @include respond-to('lg') {
    grid-template-columns: repeat(4, 1fr); // Desktop: 4 columnas
  }
}
```

**Sidebar (Admin):**
- Mobile: Panel fijo con overlay, toggle con hamburguesa
- Desktop: Sticky sidebar siempre visible

### 4.5 Patrones de Layout Adaptativos

**Container con Padding Responsive:**

```scss
.container {
  max-width: var(--container-max-width);
  margin-inline: auto;
  padding-inline: var(--spacing-4);

  @include respond-to('md') {
    padding-inline: var(--spacing-6);
  }

  @include respond-to('lg') {
    padding-inline: var(--spacing-8);
  }
}
```

**Split Layout:**

```scss
.split {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-4);

  @include respond-to('md') {
    flex-direction: row;

    > * {
      flex: 1;
    }
  }
}
```

**Utilidades de Visibilidad:**

```scss
.hide-mobile {
  @include respond-below('md') {
    display: none !important;
  }
}

.hide-desktop {
  @include respond-to('md') {
    display: none !important;
  }
}
```

---

## 5. Optimizacion Multimedia

Esta seccion documenta las mejores practicas para la gestion de imagenes y recursos multimedia en AntiPanel, siguiendo los estandares de 2025.

> **Nota:** Actualmente la aplicacion no utiliza imagenes. Esta seccion documenta la estrategia a implementar cuando se anadan recursos multimedia.

### 5.1 Formatos de Imagen Modernos

| Formato | Uso Recomendado | Soporte | Compresion |
|---------|----------------|---------|------------|
| **AVIF** | Fotografias, imagenes complejas | Chrome, Firefox, Safari 16+ | Mejor (30-50% menos que WebP) |
| **WebP** | Imagenes generales, fallback de AVIF | Universal (97%+ navegadores) | Excelente |
| **SVG** | Iconos, logos, graficos vectoriales | Universal | N/A (vectorial) |
| **PNG** | Transparencias complejas, capturas | Universal | Sin perdida |
| **JPG** | Fallback legacy, fotografias | Universal | Con perdida |

**Recomendacion:** Usar AVIF como formato principal, WebP como fallback, y JPG/PNG para navegadores legacy.

### 5.2 Elemento Picture con Srcset

El elemento `<picture>` permite servir diferentes formatos e imagenes segun el dispositivo:

```html
<picture>
  <!-- AVIF para navegadores modernos -->
  <source
    type="image/avif"
    srcset="
      imagen-400w.avif 400w,
      imagen-800w.avif 800w,
      imagen-1200w.avif 1200w
    "
    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 400px"
  />

  <!-- WebP como fallback -->
  <source
    type="image/webp"
    srcset="
      imagen-400w.webp 400w,
      imagen-800w.webp 800w,
      imagen-1200w.webp 1200w
    "
    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 400px"
  />

  <!-- JPG para navegadores legacy -->
  <img
    src="imagen-800w.jpg"
    srcset="
      imagen-400w.jpg 400w,
      imagen-800w.jpg 800w,
      imagen-1200w.jpg 1200w
    "
    sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 400px"
    alt="Descripcion detallada de la imagen"
    loading="lazy"
    decoding="async"
    width="800"
    height="600"
  />
</picture>
```

### 5.3 Lazy Loading Nativo

HTML5 proporciona lazy loading nativo sin JavaScript:

```html
<img
  src="imagen.webp"
  alt="Descripcion"
  loading="lazy"
  decoding="async"
/>
```

**Atributos importantes:**
- `loading="lazy"` - Carga la imagen solo cuando esta cerca del viewport
- `decoding="async"` - Decodifica la imagen en un hilo separado
- `fetchpriority="high"` - Para imagenes above-the-fold criticas

### 5.4 NgOptimizedImage de Angular

Angular proporciona la directiva `NgOptimizedImage` para optimizacion automatica:

```typescript
import { NgOptimizedImage } from '@angular/common';

@Component({
  imports: [NgOptimizedImage],
  template: `
    <img
      ngSrc="assets/images/hero.jpg"
      width="1200"
      height="800"
      alt="Hero image"
      priority
    />
  `
})
export class HeroComponent {}
```

**Beneficios:**
- Lazy loading automatico (excepto con `priority`)
- Preconnect automatico a CDNs de imagenes
- Validacion de width/height para evitar CLS
- Soporte para loaders de CDN (Cloudinary, Imgix, etc.)

**Configuracion con Loader personalizado:**

```typescript
// app.config.ts
import { provideImageKitLoader } from '@angular/common';

export const appConfig: ApplicationConfig = {
  providers: [
    provideImageKitLoader('https://ik.imagekit.io/tu-cuenta/')
  ]
};
```

### 5.5 Herramientas de Optimizacion

| Herramienta | Uso | URL |
|-------------|-----|-----|
| **Squoosh** | Compresion manual de imagenes, comparacion de formatos | https://squoosh.app/ |
| **SVGO/SVGOMG** | Optimizacion de SVGs | https://jakearchibald.github.io/svgomg/ |
| **Sharp** | Procesamiento de imagenes en Node.js (build-time) | https://sharp.pixelplumbing.com/ |
| **ImageMagick** | Conversion y procesamiento batch | https://imagemagick.org/ |
| **TinyPNG** | Compresion online de PNG/JPG | https://tinypng.com/ |

**Pipeline de Build Recomendado:**

```bash
# Ejemplo con Sharp para generar multiples tamanos
sharp input.jpg --resize 400 --format webp -o output-400w.webp
sharp input.jpg --resize 800 --format webp -o output-800w.webp
sharp input.jpg --resize 1200 --format webp -o output-1200w.webp
```

### 5.6 Accesibilidad de Imagenes

**Alt Text Descriptivo:**

```html
<!-- Mal -->
<img src="chart.png" alt="grafico" />

<!-- Bien -->
<img src="chart.png" alt="Grafico de barras mostrando el crecimiento de usuarios: Enero 1000, Febrero 1500, Marzo 2200" />
```

**Reglas para Alt Text:**
1. Describir el contenido y proposito de la imagen
2. Evitar "imagen de..." o "foto de..."
3. Incluir texto relevante que aparezca en la imagen
4. Para imagenes decorativas, usar `alt=""`
5. Para graficos/charts, describir los datos clave

**Imagenes Decorativas:**

```html
<!-- Imagen puramente decorativa -->
<img src="decoracion.svg" alt="" role="presentation" />

<!-- O con CSS background -->
<div class="hero" style="background-image: url(hero.jpg);" role="img" aria-label="Vista panoramica de la ciudad"></div>
```

### 5.7 Tabla Comparativa de Formatos

| Caracteristica | AVIF | WebP | PNG | JPG | SVG |
|---------------|------|------|-----|-----|-----|
| Compresion con perdida | Si | Si | No | Si | N/A |
| Compresion sin perdida | Si | Si | Si | No | N/A |
| Transparencia | Si | Si | Si | No | Si |
| Animacion | Si | Si | Si (APNG) | No | Si (SMIL) |
| Soporte HDR | Si | No | No | No | No |
| Tamano tipico (foto 1MP) | ~50KB | ~80KB | ~500KB | ~100KB | N/A |
| Soporte navegadores | 93% | 97% | 100% | 100% | 100% |

### 5.8 Estrategia de Responsive Images

**Breakpoints de Imagen:**

```scss
// Tamanos de imagen correspondientes a breakpoints CSS
$image-sizes: (
  'sm': 640px,   // Movil: imagen full-width
  'md': 768px,   // Tablet: ~50% del viewport
  'lg': 1024px,  // Desktop: ~33% del viewport
  'xl': 1280px   // Desktop grande: tamano fijo
);
```

**Sizes Attribute:**

```html
<!-- Imagen que ocupa 100% en movil, 50% en tablet, 400px en desktop -->
<img
  srcset="img-400.webp 400w, img-800.webp 800w, img-1200.webp 1200w"
  sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 400px"
  src="img-800.webp"
  alt="..."
/>
```

---

## 6. Sistema de Temas

Esta seccion documenta la arquitectura del sistema de temas de AntiPanel basado en CSS Custom Properties.

### 6.1 Arquitectura de CSS Custom Properties

AntiPanel utiliza CSS Custom Properties (variables nativas) para todos los valores de tema, permitiendo cambio de tema en tiempo de ejecucion sin recompilacion:

```scss
:root {
  // Variables de color
  --color-background: #0a0a0a;
  --color-text: #fafafa;
  // ... mas variables
}
```

**Ventajas:**
- Cambio de tema instantaneo via JavaScript
- Sin necesidad de recargar CSS
- Soporte nativo del navegador
- Cascada natural de CSS

### 6.2 Paleta Dark Mode (Actual)

El tema oscuro es el tema principal y activo por defecto:

| Token | Valor | Uso |
|-------|-------|-----|
| `--color-background` | `#0A0A0A` | Fondo principal |
| `--color-tiny-info` | `#1C1C1C` | Fondos secundarios |
| `--color-information` | `#393939` | Bordes, separadores |
| `--color-text` | `#FAFAFA` | Texto principal |
| `--color-high-contrast` | `#FFFFFF` | Texto maximo contraste |
| `--color-foreground` | `#A1A1A1` | Texto secundario |
| `--color-secondary` | `#666666` | Texto terciario, bordes |
| `--color-success` | `#00DC33` | Estados de exito, CTAs |
| `--color-error` | `#FF4444` | Estados de error |
| `--color-status-yellow` | `#F0B100` | Advertencias, pendiente |
| `--color-stats-blue` | `#00A5FF` | Estadisticas, info |

### 6.3 Paleta Light Mode (Preparada)

El tema claro esta preparado pero no activo por defecto:

| Token | Valor Dark | Valor Light |
|-------|------------|-------------|
| `--color-background` | `#0A0A0A` | `#FFFFFF` |
| `--color-tiny-info` | `#1C1C1C` | `#F5F5F5` |
| `--color-information` | `#393939` | `#E5E5E5` |
| `--color-text` | `#FAFAFA` | `#0A0A0A` |
| `--color-high-contrast` | `#FFFFFF` | `#000000` |
| `--color-foreground` | `#A1A1A1` | `#666666` |
| `--color-secondary` | `#666666` | `#999999` |
| `--color-success` | `#00DC33` | `#00B82B` |
| `--color-error` | `#FF4444` | `#DC2626` |

### 6.4 Uso de color-scheme

La propiedad `color-scheme` indica al navegador que tema usar para elementos nativos:

```scss
:root {
  color-scheme: dark; // Formularios, scrollbars, etc. en modo oscuro
}

[data-theme='light'] {
  color-scheme: light;
}
```

**Elementos afectados:**
- Scrollbars nativas
- Inputs y selects del sistema
- Dialogos nativos
- Colores de seleccion de texto

### 6.5 prefers-color-scheme Media Query

AntiPanel detecta la preferencia del sistema pero actualmente fuerza modo oscuro:

```scss
// Detecta preferencia del sistema
@media (prefers-color-scheme: light) {
  :root:not([data-theme='light']) {
    // Dark mode forzado - no se aplican cambios
    // Para respetar preferencia del sistema en el futuro:
    // Mover variables light mode aqui
  }
}
```

### 6.6 Data Attribute [data-theme]

El atributo `data-theme` en `<html>` permite cambio manual de tema:

```html
<!-- Dark mode (default) -->
<html lang="es">

<!-- Light mode (manual) -->
<html lang="es" data-theme="light">
```

**CSS correspondiente:**

```scss
// Variables dark en :root
:root {
  --color-background: #0a0a0a;
}

// Variables light cuando data-theme="light"
[data-theme='light'] {
  --color-background: #ffffff;
}
```

### 6.7 Como Activar Light Mode en el Futuro

Para habilitar el tema claro, se pueden usar tres enfoques:

**1. Respetando preferencia del sistema:**

```scss
@media (prefers-color-scheme: light) {
  :root:not([data-theme='dark']) {
    --color-background: #ffffff;
    --color-text: #0a0a0a;
    // ... resto de variables light
  }
}
```

**2. Via JavaScript (toggle manual):**

```typescript
// Servicio de tema
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private theme = signal<'dark' | 'light'>('dark');

  toggleTheme(): void {
    const newTheme = this.theme() === 'dark' ? 'light' : 'dark';
    this.theme.set(newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
  }

  initTheme(): void {
    const saved = localStorage.getItem('theme') as 'dark' | 'light' | null;
    const system = window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    const theme = saved ?? system;
    this.theme.set(theme);
    document.documentElement.setAttribute('data-theme', theme);
  }
}
```

**3. Componente de Toggle:**

```typescript
@Component({
  selector: 'app-theme-toggle',
  template: `
    <button
      type="button"
      (click)="toggleTheme()"
      [attr.aria-label]="'Cambiar a modo ' + (isDark() ? 'claro' : 'oscuro')"
    >
      <ng-icon [name]="isDark() ? 'matLightMode' : 'matDarkMode'" />
    </button>
  `
})
export class ThemeToggle {
  private themeService = inject(ThemeService);
  isDark = computed(() => this.themeService.theme() === 'dark');

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
```

---

## 7. Informe de Accesibilidad

Esta seccion documenta las practicas de accesibilidad implementadas en AntiPanel siguiendo las pautas WCAG 2.1.

### 7.1 Nivel de Conformidad WCAG

AntiPanel apunta a conformidad **WCAG 2.1 Nivel AA**, que incluye:

- Todos los criterios de Nivel A
- Todos los criterios de Nivel AA
- Criterios seleccionados de Nivel AAA donde sea practico

**Criterios clave implementados:**
- 1.1.1 Contenido no textual (Nivel A)
- 1.3.1 Informacion y relaciones (Nivel A)
- 1.4.3 Contraste minimo (Nivel AA)
- 2.1.1 Teclado (Nivel A)
- 2.4.7 Foco visible (Nivel AA)
- 4.1.2 Nombre, rol, valor (Nivel A)

### 7.2 Contraste de Colores

Todos los colores de texto cumplen con los ratios WCAG AA:

| Combinacion | Ratio | Requisito AA | Estado |
|-------------|-------|--------------|--------|
| Text (#FAFAFA) sobre Background (#0A0A0A) | 19.4:1 | 4.5:1 | ✓ Pasa |
| Foreground (#A1A1A1) sobre Background | 8.5:1 | 4.5:1 | ✓ Pasa |
| Success (#00DC33) sobre Background | 8.2:1 | 4.5:1 | ✓ Pasa |
| Error (#FF4444) sobre Background | 5.3:1 | 4.5:1 | ✓ Pasa |

**Herramientas de verificacion:**
- [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)
- [Colour Contrast Analyser](https://www.tpgi.com/color-contrast-checker/)
- Chrome DevTools > Accessibility panel

### 7.3 Navegacion por Teclado

Todos los elementos interactivos son accesibles via teclado:

| Tecla | Accion |
|-------|--------|
| `Tab` | Navegar al siguiente elemento focusable |
| `Shift + Tab` | Navegar al elemento anterior |
| `Enter` | Activar botones y enlaces |
| `Space` | Activar botones, checkboxes, toggles |
| `Escape` | Cerrar modales y menus |
| `Arrow Keys` | Navegar dentro de menus y selects |

**Orden de tabulacion:**
- Sigue el orden visual del DOM
- Skip link como primer elemento focusable
- No hay "tab traps" (excepto modales activos)

### 7.4 Focus Visible y Skip Links

**Focus Ring:**

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
```

**Variables de focus:**

```scss
--focus-ring-width: 2px;
--focus-ring-offset: 2px;
--focus-ring-color: var(--color-stats-blue); // #00A5FF
```

**Skip Link:**

```html
<a href="#main-content" class="skip-link">
  Saltar al contenido principal
</a>
```

```scss
.skip-link {
  position: absolute;
  left: -9999px;

  &:focus {
    left: var(--spacing-4);
    top: var(--spacing-4);
    z-index: var(--z-tooltip);
    // ... estilos visibles
  }
}
```

### 7.5 ARIA Attributes Utilizados

**Landmarks:**

```html
<header role="banner">...</header>
<nav role="navigation" aria-label="Main navigation">...</nav>
<main role="main" id="main-content">...</main>
<aside role="complementary" aria-label="Admin navigation">...</aside>
<footer role="contentinfo">...</footer>
```

**Estados dinamicos:**

```html
<!-- Menu expandible -->
<button
  aria-expanded="false"
  aria-controls="mobile-menu"
  aria-label="Toggle navigation menu"
>

<!-- Input con error -->
<input
  aria-invalid="true"
  aria-describedby="email-error"
/>
<p id="email-error" role="alert">Email invalido</p>

<!-- Boton cargando -->
<button aria-busy="true" aria-label="Enviando...">

<!-- Notificacion -->
<div role="alert" aria-live="polite">Guardado exitosamente</div>
```

### 7.6 Reduced Motion Support

AntiPanel respeta la preferencia de movimiento reducido:

```scss
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

**Smooth scroll condicional:**

```scss
html {
  @media (prefers-reduced-motion: no-preference) {
    scroll-behavior: smooth;
  }
}
```

### 7.7 Semantic HTML Landmarks

AntiPanel utiliza elementos semanticos HTML5:

| Elemento | Proposito | Cantidad Maxima |
|----------|-----------|-----------------|
| `<header>` | Cabecera del sitio o seccion | 1 por pagina (banner) |
| `<nav>` | Navegacion principal y secundaria | Multiples con aria-label |
| `<main>` | Contenido principal | 1 por pagina |
| `<aside>` | Contenido complementario | Multiples |
| `<footer>` | Pie de pagina | 1 por pagina |
| `<article>` | Contenido independiente | Multiples |
| `<section>` | Seccion generica con heading | Multiples |

### 7.8 Formularios Accesibles

**Estructura de Form Input:**

```html
<fieldset class="form-input">
  <label for="email" class="form-input__label">
    Email
    <span class="form-input__required" aria-hidden="true">*</span>
    <span class="sr-only">(requerido)</span>
  </label>

  <input
    id="email"
    type="email"
    class="form-input__field"
    aria-invalid="true"
    aria-describedby="email-error email-hint"
    required
  />

  <p id="email-hint" class="form-input__hint">
    Usaremos tu email para notificaciones
  </p>

  <p id="email-error" class="form-input__error" role="alert">
    Por favor ingresa un email valido
  </p>
</fieldset>
```

**Patrones clave:**
- Labels asociados via `for`/`id`
- `aria-invalid` para estado de validacion
- `aria-describedby` para errores y hints
- `role="alert"` para errores dinamicos
- Texto "(requerido)" para screen readers

### 7.9 Checklist de Accesibilidad

**Antes de cada release, verificar:**

- [ ] Todos los elementos interactivos son accesibles via teclado
- [ ] Focus visible en todos los elementos focusables
- [ ] Contraste de color cumple WCAG AA (4.5:1 texto, 3:1 elementos grandes)
- [ ] Todas las imagenes tienen alt text apropiado
- [ ] Formularios tienen labels asociados
- [ ] Errores de formulario son anunciados
- [ ] Skip link funciona correctamente
- [ ] Reduced motion es respetado
- [ ] Estructura de headings es logica (h1 > h2 > h3...)
- [ ] ARIA roles y estados son correctos
- [ ] Modales atrapan el foco correctamente
- [ ] No hay contenido que parpadee > 3 veces/segundo

**Herramientas de Testing:**
- axe DevTools (extension Chrome/Firefox)
- Lighthouse Accessibility audit
- WAVE Web Accessibility Evaluator
- NVDA / VoiceOver para testing con screen reader

---

## Resumen

AntiPanel Frontend implementa un sistema de diseno completo con:

1. **Arquitectura CSS ITCSS** con design tokens
2. **HTML Semantico** para accesibilidad
3. **Componentes Angular 21** con signals y control flow moderno
4. **BEM** para nomenclatura de clases
5. **ngicons** para iconografia
6. **Style Guide** para documentacion visual
7. **Responsive Design** mobile-first con 5 breakpoints
8. **Sistema de Temas** preparado para dark/light mode
9. **Accesibilidad WCAG AA** con soporte completo de teclado

Para mas informacion, consulta el codigo fuente de cada componente.
