# JUSTIFICACION DE RESULTADOS DE APRENDIZAJE
## Fases 1, 2 y 3 - Desarrollo Frontend con Angular

---

**Proyecto:** AntiPanel Frontend
**Framework:** Angular 21 (con Signals, zoneless change detection)
**Paginas evaluadas:** `/style-guide` y `/cliente`
**Fecha de evaluacion:** Enero 2026
**Repositorio:** [frontend/src/app/](../../src/app/)

---

## INDICE DE CONTENIDOS

### Navegacion Rapida por Criterio de Evaluacion

| Criterio | Descripcion | Puntos | Ir a seccion |
|:--------:|-------------|:------:|:------------:|
| **CE6.a** | Separacion contenido/aspecto/comportamiento | 10/10 | [Ver CE6.a](#ce6a-separacion-de-contenido-aspecto-y-comportamiento) |
| **CE6.c** | Manipulacion del DOM | 24-25/30 | [Ver CE6.c](#ce6c-manipulacion-del-dom) |
| **CE6.d** | Sistema de eventos | 37-38/40 | [Ver CE6.d](#ce6d-sistema-de-eventos) |
| **CE6.e** | Componentes interactivos | 43-45/50 | [Ver CE6.e](#ce6e-componentes-interactivos) |
| **CE6.g** | Documentacion tecnica | 18-19/30 | [Ver CE6.g](#ce6g-documentacion-tecnica) |
| **CE6.h** | Theme Switcher | 10/10 | [Ver CE6.h](#ce6h-theme-switcher) |

### Indice Detallado

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [CE6.a - Separacion de Contenido/Aspecto/Comportamiento](#ce6a-separacion-de-contenido-aspecto-y-comportamiento)
3. [CE6.c - Manipulacion del DOM](#ce6c-manipulacion-del-dom)
   - [1.1 ViewChild y ElementRef](#11-viewchild-y-elementref)
   - [1.2 Renderer2 - Modificacion de estilos](#12-renderer2---modificacion-de-estilos)
   - [1.3 Renderer2 - Creacion/eliminacion de elementos](#13-renderer2---creacioneliminacion-de-elementos)
4. [CE6.d - Sistema de Eventos](#ce6d-sistema-de-eventos)
   - [2.1 Event binding en componentes](#21-event-binding-en-componentes)
   - [2.2 Eventos de teclado, mouse y focus](#22-eventos-de-teclado-mouse-y-focus)
   - [2.3 preventDefault y stopPropagation](#23-preventdefault-y-stoppropagation)
   - [2.4 @HostListener para eventos globales](#24-hostlistener-para-eventos-globales)
5. [CE6.e - Componentes Interactivos](#ce6e-componentes-interactivos)
   - [3.1 Menu hamburguesa](#31-menu-hamburguesa)
   - [3.2 Modal](#32-modal)
   - [3.3 Accordion](#33-accordion)
   - [3.4 Tabs](#34-tabs)
   - [3.5 Tooltip](#35-tooltip)
6. [CE6.g - Documentacion Tecnica](#ce6g-documentacion-tecnica)
   - [5.1 Seccion de arquitectura](#51-seccion-de-arquitectura-de-eventos)
   - [5.2 Diagrama de flujo](#52-diagrama-de-flujo-de-eventos)
   - [5.3 Tabla de compatibilidad](#53-tabla-de-compatibilidad-de-navegadores)
7. [CE6.h - Theme Switcher](#ce6h-theme-switcher)
8. [Archivos de Referencia](#archivos-de-referencia)
9. [Decisiones Arquitectonicas Angular 21](#decisiones-arquitectonicas-angular-21)
   - [Por que Renderer2 solo en TooltipDirective](#por-que-renderer2-solo-en-tooltipdirective)
   - [Por que Accordion usa HTML5 nativo](#por-que-accordion-usa-html5-nativo)
   - [Por que no tabla de compatibilidad](#por-que-no-tabla-de-compatibilidad-de-navegadores)
10. [Conclusion](#conclusion)

---

## RESUMEN EJECUTIVO

### Puntuacion Total Estimada: **143-148 / 170 puntos (84-87%)**

| CE | Criterios | Obtenido | Maximo | % |
|:--:|-----------|:--------:|:------:|:-:|
| CE6.a | Separacion responsabilidades | **10** | 10 | 100% |
| CE6.c | Manipulacion DOM (1.1, 1.2, 1.3) | **24-25** | 30 | 80-83% |
| CE6.d | Eventos (2.1, 2.2, 2.3, 2.4) | **37-38** | 40 | 93-95% |
| CE6.e | Componentes (3.1-3.5) | **43-45** | 50 | 86-90% |
| CE6.g | Documentacion (5.1, 5.2, 5.3) | **18-19** | 30 | 60-63% |
| CE6.h | Theme Switcher | **10** | 10 | 100% |
| | **TOTAL** | **143-148** | **170** | **84-87%** |

### Puntos Principales Perdidos

| Criterio | Puntos Perdidos | Razon |
|----------|:---------------:|-------|
| 5.3 Tabla navegadores | **-10** | No existe tabla de compatibilidad |
| 1.2 Renderer2 estilos | **-2 a -3** | Renderer2 solo en 1 componente (ver [Decisiones Arquitectonicas](#decisiones-arquitectonicas-angular-21)) |
| 1.3 Crear/eliminar DOM | **-3** | Solo 1 componente con Renderer2 (ver [Decisiones Arquitectonicas](#decisiones-arquitectonicas-angular-21)) |
| 3.3 Accordion | **-3 a -4** | Usa HTML5 nativo (ver [Decisiones Arquitectonicas](#decisiones-arquitectonicas-angular-21)) |

---

## CE6.a: SEPARACION DE CONTENIDO, ASPECTO Y COMPORTAMIENTO

### Puntuacion: **10/10**

### Requisitos de la Rubrica

| Requisito | Cumple | Evidencia |
|-----------|:------:|-----------|
| HTML solo contiene estructura semantica | ✅ | Archivos `.html` separados |
| CSS/SCSS en archivos independientes | ✅ | Archivos `.scss` por componente |
| Logica en archivos TypeScript | ✅ | Archivos `.ts` separados |
| 0% estilos inline no justificados | ✅ | Solo `[style.xxx]` dinamico |
| 0% onclick en HTML | ✅ | Usa `(click)="handler()"` |
| Servicios para logica de negocio | ✅ | Servicios inyectables |

### Estructura de Archivos - Evidencia

```
frontend/src/app/
├── components/
│   └── shared/
│       └── modal/
│           ├── modal.ts       ← Logica (comportamiento)
│           ├── modal.html     ← Estructura (contenido)
│           └── modal.scss     ← Estilos (aspecto)
├── services/
│   ├── theme.service.ts       ← Logica de negocio
│   ├── notification.service.ts
│   └── loading.service.ts
└── directives/
    └── tooltip.directive.ts
```

### Codigo de Ejemplo - Separacion Correcta

**Archivo:** [`src/app/services/theme.service.ts`](../../src/app/services/theme.service.ts) (lineas 23-26)

```typescript
// ✅ CORRECTO: Logica de negocio en servicio inyectable
@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  readonly currentTheme = computed<ResolvedTheme>(() => {
    return this.selectedTheme() === 'system'
      ? this.systemTheme()
      : this.selectedTheme() as ResolvedTheme;
  });
}
```

**Archivo:** [`src/app/components/layout/header/header.ts`](../../src/app/components/layout/header/header.ts) (linea 45)

```typescript
// ✅ CORRECTO: Componente delega logica al servicio
export class Header {
  protected readonly themeService = inject(ThemeService);

  protected onThemeToggle(): void {
    this.themeService.toggleTheme();  // Delega al servicio
  }
}
```

### Justificacion de la Puntuacion

**10/10** porque:
- Separacion total entre `.ts`, `.html` y `.scss` en todos los componentes
- Cero estilos inline no justificados
- Cero manejadores de eventos inline (`onclick=""`)
- Logica de negocio correctamente delegada a servicios
- CSS Custom Properties para tematizacion (ver [`src/styles/_variables.scss`](../../src/styles/_variables.scss))

---

## CE6.c: MANIPULACION DEL DOM

### Puntuacion Total: **24-25/30**

---

### 1.1 ViewChild y ElementRef

#### Puntuacion: **10/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa ViewChild/ElementRef | ❌ |
| 2-4 | Implementa en 1 componente | ❌ |
| 5-7 | Implementa en 2 componentes | ❌ |
| 8-9 | Implementa en 3+ componentes | ❌ |
| **10** | **Implementa en 5+ componentes con uso avanzado** | ✅ |

#### Componentes que Implementan ViewChild/ElementRef

| # | Archivo | Linea | Codigo |
|:-:|---------|:-----:|--------|
| 1 | [`dom-events-section.ts`](../../src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts) | 48-51 | `viewChild<ElementRef<HTMLInputElement>>('demoInput')` |
| 2 | [`modal.ts`](../../src/app/components/shared/modal/modal.ts) | 56 | `viewChild<ElementRef<HTMLDialogElement>>('dialogRef')` |
| 3 | [`header.ts`](../../src/app/components/layout/header/header.ts) | 75-78 | `viewChild<ElementRef<HTMLElement>>('profileContainer')` |
| 4 | [`tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts) | 33 | `inject(ElementRef)` |
| 5 | [`order-ready.ts`](../../src/app/components/shared/order-ready/order-ready.ts) | 96-99 | `viewChild<ElementRef<HTMLInputElement>>('targetInput')` |

#### Evidencia de Codigo

**Archivo:** [`src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts`](../../src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts)

```typescript
// Lineas 47-65
/** Referencia al input para focus programatico */
protected readonly demoInput = viewChild<ElementRef<HTMLInputElement>>('demoInput');

/** Referencia al contenedor para manipular estilos */
protected readonly demoBox = viewChild<ElementRef<HTMLDivElement>>('demoBox');

protected focusInput(): void {
  const input = this.demoInput();
  if (input) {
    input.nativeElement.focus();  // ← Acceso a nativeElement
    this.notificationService.info('Input enfocado via ViewChild');
  }
}
```

**Archivo:** [`src/app/components/shared/modal/modal.ts`](../../src/app/components/shared/modal/modal.ts)

```typescript
// Lineas 56-76
private readonly dialogRef = viewChild<ElementRef<HTMLDialogElement>>('dialogRef');

constructor() {
  effect(() => {
    const dialog = this.dialogRef()?.nativeElement;  // ← Acceso seguro
    if (!dialog) return;

    if (this.isOpen()) {
      dialog.showModal();  // ← Metodo nativo del DOM
      this.document.body.style.overflow = 'hidden';
    }
  });
}
```

**Archivo:** [`src/app/components/shared/order-ready/order-ready.ts`](../../src/app/components/shared/order-ready/order-ready.ts)

```typescript
// Lineas 96-118
protected readonly targetInputRef = viewChild<ElementRef<HTMLInputElement>>('targetInput');
protected readonly quantityInputRef = viewChild<ElementRef<HTMLInputElement>>('quantityInput');

constructor() {
  // Auto-focus con afterNextRender (Angular 21 best practice)
  effect(() => {
    if (this.isEditingTarget()) {
      afterNextRender(() => {
        this.targetInputRef()?.nativeElement.focus();  // ← Focus programatico
      }, { injector: this.injector });
    }
  });
}
```

#### Justificacion de la Puntuacion

**10/10** porque:
- ✅ ViewChild/ElementRef en **5 componentes diferentes**
- ✅ Usa `viewChild()` signal API (Angular 21 moderno)
- ✅ Acceso correcto a `nativeElement`
- ✅ Uso de `afterNextRender` para acceso seguro al DOM
- ✅ Documentado en [DOCUMENTACION.md](./DOCUMENTACION.md) seccion 1.1

---

### 1.2 Renderer2 - Modificacion de Estilos

#### Puntuacion: **7-8/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No usa Renderer2 | ❌ |
| 2-4 | Usa Renderer2 en 1 componente basico | ❌ |
| 5-7 | Usa Renderer2 en 1-2 componentes con multiples metodos | ⚠️ |
| 8-9 | Usa Renderer2 en 3+ componentes sin manipulacion directa | ❌ |
| 10 | Extensivo en 4+ componentes, 0% manipulacion directa | ❌ |

#### Uso de Renderer2 en el Proyecto

**Archivo principal:** [`src/app/directives/tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts)

| Metodo Renderer2 | Linea | Uso |
|------------------|:-----:|-----|
| `inject(Renderer2)` | 34 | Inyeccion del servicio |
| `createElement()` | 97 | Crear elemento tooltip |
| `setAttribute()` | 98, 99, 114 | 3 usos diferentes |
| `addClass()` | 100, 101, 119 | 3 usos diferentes |
| `createText()` | 104 | Crear nodo de texto |
| `appendChild()` | 105, 108 | 2 usos diferentes |
| `removeAttribute()` | 126 | Limpiar atributos |
| `removeChild()` | 127 | Eliminar del DOM |
| `setStyle()` | 175, 176 | Posicionamiento dinamico |

**Total: 14+ llamadas a metodos Renderer2**

#### Evidencia de Codigo

**Archivo:** [`src/app/directives/tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts)

```typescript
// Linea 34 - Inyeccion
private readonly renderer = inject(Renderer2);

// Lineas 95-122 - Creacion de tooltip
private createTooltip(): void {
  // createElement - Crear elemento
  this.tooltipElement = this.renderer.createElement('div');

  // setAttribute - Atributos de accesibilidad
  this.renderer.setAttribute(this.tooltipElement, 'id', this.tooltipId);
  this.renderer.setAttribute(this.tooltipElement, 'role', 'tooltip');

  // addClass - Clases CSS
  this.renderer.addClass(this.tooltipElement, 'app-tooltip');
  this.renderer.addClass(this.tooltipElement, `app-tooltip--${this.tooltipPosition()}`);

  // createText + appendChild - Contenido
  const text = this.renderer.createText(this.appTooltip());
  this.renderer.appendChild(this.tooltipElement, text);

  // appendChild - Agregar al body
  this.renderer.appendChild(document.body, this.tooltipElement);

  // setAttribute - aria-describedby
  this.renderer.setAttribute(this.el.nativeElement, 'aria-describedby', this.tooltipId);

  // addClass - Animacion
  requestAnimationFrame(() => {
    if (this.tooltipElement) {
      this.renderer.addClass(this.tooltipElement, 'app-tooltip--visible');
    }
  });
}

// Lineas 175-176 - setStyle para posicionamiento
this.renderer.setStyle(this.tooltipElement, 'top', `${top}px`);
this.renderer.setStyle(this.tooltipElement, 'left', `${left}px`);
```

#### Manipulacion Directa (resta puntos)

| Archivo | Linea | Codigo | Justificacion |
|---------|:-----:|--------|---------------|
| [`modal.ts`](../../src/app/components/shared/modal/modal.ts) | 68 | `this.document.body.style.overflow = 'hidden'` | Bloqueo scroll |
| [`theme.service.ts`](../../src/app/services/theme.service.ts) | 67 | `document.documentElement.setAttribute(...)` | Cambio tema |

#### Justificacion de la Puntuacion

**7-8/10** porque:
- ✅ Uso extensivo de Renderer2 (14+ llamadas)
- ✅ Multiples metodos: `createElement`, `setAttribute`, `addClass`, `setStyle`, `appendChild`, `removeChild`
- ⚠️ Solo 1 componente usa Renderer2 (TooltipDirective)
- ❌ Existe manipulacion directa en `modal.ts` y `theme.service.ts`

**Para obtener 10/10:** Migrar manipulacion directa a Renderer2 en modal.ts y theme.service.ts

---

### 1.3 Renderer2 - Creacion/Eliminacion de Elementos

#### Puntuacion: **7/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No crea/elimina elementos | ❌ |
| 2-4 | Crea elementos pero no limpia | ❌ |
| 5-7 | Crea/elimina en 1 componente con lifecycle | ✅ |
| 8-9 | Crea/elimina en 2+ componentes | ❌ |
| 10 | Crea/elimina en 3+ componentes con cleanup perfecto | ❌ |

#### Evidencia de Codigo

**Archivo:** [`src/app/directives/tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts)

**Creacion de elementos (lineas 95-108):**

```typescript
private createTooltip(): void {
  // createElement - Crear div
  this.tooltipElement = this.renderer.createElement('div');

  // createText - Crear nodo de texto
  const text = this.renderer.createText(this.appTooltip());

  // appendChild - Agregar texto al tooltip
  this.renderer.appendChild(this.tooltipElement, text);

  // appendChild - Agregar tooltip al body
  this.renderer.appendChild(document.body, this.tooltipElement);
}
```

**Eliminacion de elementos (lineas 124-129):**

```typescript
private removeTooltip(): void {
  if (this.tooltipElement) {
    // removeAttribute - Limpiar aria
    this.renderer.removeAttribute(this.el.nativeElement, 'aria-describedby');

    // removeChild - Eliminar del DOM
    this.renderer.removeChild(document.body, this.tooltipElement);

    this.tooltipElement = null;
  }
}
```

**Gestion del ciclo de vida (lineas 74-79):**

```typescript
ngOnDestroy(): void {
  // Limpiar timeout pendiente
  if (this.showTimeout) {
    clearTimeout(this.showTimeout);
  }
  // Eliminar tooltip si existe
  this.hide();  // ← Llama a removeTooltip()
}
```

#### Justificacion de la Puntuacion

**7/10** porque:
- ✅ Crea elementos con `createElement`, `createText`, `appendChild`
- ✅ Elimina elementos con `removeChild`
- ✅ Limpia en `ngOnDestroy` (gestion correcta del ciclo de vida)
- ❌ Solo 1 componente implementa creacion/eliminacion

**Para obtener 10/10:** Implementar creacion dinamica con Renderer2 en 2+ componentes adicionales

---

## CE6.d: SISTEMA DE EVENTOS

### Puntuacion Total: **37-38/40**

---

### 2.1 Event Binding en Componentes

#### Puntuacion: **10/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa event binding | ❌ |
| 2-4 | Solo click en 1-2 componentes | ❌ |
| 5-7 | 3-5 tipos de eventos | ❌ |
| 8-9 | 6-8 tipos de eventos | ❌ |
| **10** | **9+ tipos de eventos diferentes** | ✅ |

#### Eventos Implementados (11+ tipos)

| # | Evento | Archivo | Template |
|:-:|--------|---------|----------|
| 1 | `(click)` | Multiples | `<button (click)="onButtonClick()">` |
| 2 | `(keydown)` | dom-events-section | `<input (keydown)="onKeyDown($event)">` |
| 3 | `(keydown.enter)` | order-ready, auth-form | `<input (keydown.enter)="onSubmit()">` |
| 4 | `(keydown.escape)` | modal, tooltip | `(keydown.escape)="onCancel()"` |
| 5 | `(mouseenter)` | tooltip.directive | `@HostListener('mouseenter')` |
| 6 | `(mouseleave)` | tooltip.directive | `@HostListener('mouseleave')` |
| 7 | `(focus)` | dom-events-section | `<input (focus)="onFocus()">` |
| 8 | `(blur)` | auth-form | `<input (blur)="onBlur()">` |
| 9 | `(submit)` | dom-events-section | `<form (submit)="onFormSubmit($event)">` |
| 10 | `(mousemove)` | dom-events-section | `<div (mousemove)="onMouseMove($event)">` |
| 11 | `(input)` | auth-form | `<input (input)="onConfirmPasswordInput()">` |

#### Evidencia de Codigo

**Archivo:** [`src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts`](../../src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts)

```typescript
// Lineas 104-122 - Multiples manejadores de eventos
protected onButtonClick(): void {
  this.clickCount.update(c => c + 1);
}

protected onKeyDown(event: KeyboardEvent): void {
  this.lastKey.set(event.key);
}

protected onFocus(): void {
  this.hasFocus.set(true);
}

protected onBlur(): void {
  this.hasFocus.set(false);
}

protected onMouseMove(event: MouseEvent): void {
  this.mousePosition.set({ x: event.offsetX, y: event.offsetY });
}
```

#### Justificacion de la Puntuacion

**10/10** porque:
- ✅ 11+ tipos de eventos diferentes implementados
- ✅ Eventos en multiples componentes
- ✅ Uso correcto de `$event` para acceder a datos del evento
- ✅ Eventos especificos de teclado (`keydown.enter`, `keydown.escape`)

---

### 2.2 Eventos de Teclado, Mouse y Focus

#### Puntuacion: **10/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa estos eventos | ❌ |
| 2-4 | Solo 1 categoria | ❌ |
| 5-7 | 2 categorias | ❌ |
| 8-9 | 3 categorias basico | ❌ |
| **10** | **3 categorias con uso avanzado** | ✅ |

#### Desglose por Categoria

**Eventos de Teclado (5 tipos):**

| Evento | Archivo | Linea | Contexto |
|--------|---------|:-----:|----------|
| `keydown` | dom-events-section.ts | 108 | Captura cualquier tecla |
| `keydown.enter` | order-ready.ts | 171 | Submit de input |
| `keydown.escape` | tooltip.directive.ts | 69-72 | Cerrar tooltip |
| `ArrowUp/ArrowDown` | header.ts | 222-230 | Navegacion dropdown |
| `Tab/Shift+Tab` | modal.ts | 104-127 | Focus trap |

**Eventos de Mouse (4 tipos):**

| Evento | Archivo | Linea | Contexto |
|--------|---------|:-----:|----------|
| `click` | Multiples | - | Interaccion general |
| `mouseenter` | tooltip.directive.ts | 51-52 | Mostrar tooltip |
| `mouseleave` | tooltip.directive.ts | 59-60 | Ocultar tooltip |
| `mousemove` | dom-events-section.ts | 120 | Tracking posicion |

**Eventos de Focus (2 tipos):**

| Evento | Archivo | Linea | Contexto |
|--------|---------|:-----:|----------|
| `focus` | tooltip.directive.ts | 52 | Mostrar tooltip con Tab |
| `blur` | auth-form.ts | 341-350 | Validacion al perder foco |

#### Evidencia de Codigo - Navegacion por Teclado

**Archivo:** [`src/app/components/layout/header/header.ts`](../../src/app/components/layout/header/header.ts)

```typescript
// Lineas 216-240 - Navegacion WAI-ARIA completa
protected onDropdownKeydown(event: KeyboardEvent): void {
  const items = this.dropdownItems();
  if (!items.length) return;

  const currentIndex = this.getCurrentDropdownItemIndex(items);

  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault();
      this.focusDropdownItem(items, currentIndex + 1);
      break;
    case 'ArrowUp':
      event.preventDefault();
      this.focusDropdownItem(items, currentIndex - 1);
      break;
    case 'Escape':
      event.preventDefault();
      this.closeProfileDropdown();
      break;
    case 'Tab':
      this.closeProfileDropdown();
      break;
  }
}
```

#### Justificacion de la Puntuacion

**10/10** porque:
- ✅ Eventos de teclado: 5 tipos incluyendo navegacion con flechas
- ✅ Eventos de mouse: 4 tipos incluyendo tracking de posicion
- ✅ Eventos de focus: 2 tipos con validacion al blur
- ✅ Navegacion WAI-ARIA completa en dropdown

---

### 2.3 preventDefault y stopPropagation

#### Puntuacion: **9/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No los usa | ❌ |
| 2-4 | Usa 1 en 1 contexto | ❌ |
| 5-7 | Usa ambos en 1-2 contextos | ❌ |
| 8-9 | Usa ambos en 3+ contextos | ✅ |
| 10 | Uso avanzado en 4+ contextos con documentacion | ❌ |

#### Usos de preventDefault()

| # | Archivo | Linea | Contexto |
|:-:|---------|:-----:|----------|
| 1 | dom-events-section.ts | 132 | Evitar navegacion de link |
| 2 | dom-events-section.ts | 137 | Evitar recarga de formulario |
| 3 | modal.ts | 98 | Evitar cierre nativo con ESC |
| 4 | modal.ts | 117, 123 | Focus trap con Tab |
| 5 | header.ts | 224, 228, 232 | Navegacion por teclado |

#### Usos de stopPropagation()

| # | Archivo | Linea | Contexto |
|:-:|---------|:-----:|----------|
| 1 | dom-events-section.ts | 147 | Evitar propagacion a contenedor padre |

#### Evidencia de Codigo

**Archivo:** [`src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts`](../../src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts)

```typescript
// Lineas 131-149
protected onLinkClick(event: MouseEvent): void {
  event.preventDefault();  // ← Evita navegacion del link
  this.addToLog('Link click - preventDefault() aplicado');
}

protected onFormSubmit(event: SubmitEvent): void {
  event.preventDefault();  // ← Evita recarga de pagina
  this.addToLog('Form submit - preventDefault() aplicado');
  this.notificationService.success('Formulario procesado sin recargar');
}

protected onInnerClick(event: MouseEvent): void {
  event.stopPropagation();  // ← Evita propagacion al padre
  this.addToLog('Click en boton interior - stopPropagation() aplicado');
}
```

**Archivo:** [`src/app/components/shared/modal/modal.ts`](../../src/app/components/shared/modal/modal.ts)

```typescript
// Lineas 96-127 - Focus trap con preventDefault
protected onKeydown(event: KeyboardEvent): void {
  if (event.key === 'Escape' && this.closeOnEsc()) {
    event.preventDefault();  // ← Evita cierre nativo de <dialog>
    this.closeRequest.emit();
    return;
  }

  if (event.key === 'Tab') {
    // Focus trap
    if (event.shiftKey) {
      if (this.document.activeElement === firstElement) {
        event.preventDefault();  // ← Evita salir del modal
        lastElement.focus();
      }
    } else {
      if (this.document.activeElement === lastElement) {
        event.preventDefault();  // ← Evita salir del modal
        firstElement.focus();
      }
    }
  }
}
```

#### Justificacion de la Puntuacion

**9/10** porque:
- ✅ `preventDefault()` en 5+ contextos diferentes
- ✅ `stopPropagation()` implementado correctamente
- ✅ Uso avanzado en focus trap del modal
- ⚠️ Falta documentacion explicita de cada uso

---

### 2.4 @HostListener para Eventos Globales

#### Puntuacion: **8/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No usa @HostListener | ❌ |
| 2-4 | 1 componente con 1 evento | ❌ |
| 5-7 | 1-2 componentes con multiples eventos | ❌ |
| 8-9 | 2+ componentes con eventos globales | ✅ |
| 10 | 3+ componentes con eventos globales avanzados | ❌ |

#### Componentes con @HostListener

| # | Archivo | Eventos | Descripcion |
|:-:|---------|---------|-------------|
| 1 | header.ts | `document:click` | Cerrar dropdown al click fuera |
| 2 | tooltip.directive.ts | `mouseenter`, `focus`, `mouseleave`, `blur`, `keydown.escape` | Control del tooltip |

#### Evidencia de Codigo

**Archivo:** [`src/app/components/layout/header/header.ts`](../../src/app/components/layout/header/header.ts)

```typescript
// Lineas 194-206 - Evento global document:click
@HostListener('document:click', ['$event'])
onDocumentClick(event: MouseEvent): void {
  if (!this.isProfileDropdownOpen()) return;

  const target = event.target as HTMLElement;
  const container = this.profileContainerRef()?.nativeElement;

  // Cerrar si el click es fuera del contenedor
  if (container && !container.contains(target)) {
    this.closeProfileDropdown();
  }
}
```

**Archivo:** [`src/app/directives/tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts)

```typescript
// Lineas 51-72 - Multiples @HostListener
@HostListener('mouseenter')
@HostListener('focus')
onShowTooltip(): void {
  this.showTimeout = setTimeout(() => {
    this.show();
  }, this.tooltipDelay());
}

@HostListener('mouseleave')
@HostListener('blur')
onHideTooltip(): void {
  if (this.showTimeout) {
    clearTimeout(this.showTimeout);
    this.showTimeout = null;
  }
  this.hide();
}

@HostListener('keydown.escape')
onEscape(): void {
  this.hide();
}
```

#### Justificacion de la Puntuacion

**8/10** porque:
- ✅ 2 componentes usan @HostListener
- ✅ Evento global `document:click` para click fuera
- ✅ Multiples eventos en tooltip (5 @HostListener)
- ❌ Falta `window:resize` o similar en un tercer componente

**Para obtener 10/10:** Agregar @HostListener en un componente adicional

---

## CE6.e: COMPONENTES INTERACTIVOS

### Puntuacion Total: **43-45/50**

---

### 3.1 Menu Hamburguesa

#### Puntuacion: **8-9/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa menu mobile | ❌ |
| 2-4 | Toggle basico sin animacion | ❌ |
| 5-7 | Toggle + animacion + click fuera | ❌ |
| **8-9** | **Anterior + ESC + aria-expanded** | ✅ |
| 10 | Completo con focus trap | ❌ |

#### Funcionalidades Implementadas

| Funcionalidad | Estado | Evidencia |
|---------------|:------:|-----------|
| Toggle abrir/cerrar | ✅ | `isMobileMenuOpen.update(open => !open)` |
| Animacion CSS | ✅ | Clases CSS condicionales |
| Click fuera cierra | ✅ | `@HostListener('document:click')` |
| aria-expanded | ✅ | Binding dinamico |
| **Cierre con ESC** | ✅ | `@HostListener('document:keydown.escape')` |
| Focus trap | ❌ | No implementado (opcional para 10/10) |

#### Evidencia de Codigo

**Archivo:** [`src/app/components/layout/header/header.ts`](../../src/app/components/layout/header/header.ts)

```typescript
// Lineas 69, 147-153, 208-221
protected readonly isMobileMenuOpen = signal(false);

protected toggleMobileMenu(): void {
  this.isMobileMenuOpen.update(open => !open);
}

protected closeMobileMenu(): void {
  this.isMobileMenuOpen.set(false);
}

/**
 * Handles global ESC key to close mobile menu and dropdown.
 * WCAG 2.1.1 (Keyboard), 2.1.2 (No Keyboard Trap)
 */
@HostListener('document:keydown.escape')
onGlobalEscape(): void {
  if (this.isMobileMenuOpen()) {
    this.closeMobileMenu();
  }
  if (this.isProfileDropdownOpen()) {
    this.closeProfileDropdown();
  }
}
```

#### Justificacion de la Puntuacion

**8-9/10** porque:
- ✅ Toggle funcional con signal
- ✅ Click fuera cierra menu (`@HostListener('document:click')`)
- ✅ Animacion CSS
- ✅ aria-expanded binding
- ✅ **Cierre con ESC** (WCAG 2.1.1, 2.1.2)
- ❌ Falta focus trap (solo requerido para 10/10)

---

### 3.2 Modal

#### Puntuacion: **10/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa modal | ❌ |
| 2-4 | Modal basico sin accesibilidad | ❌ |
| 5-7 | Modal con ESC y overlay click | ❌ |
| 8-9 | Anterior + scroll lock + focus management | ❌ |
| **10** | **Completo con focus trap y restore focus** | ✅ |

#### Funcionalidades Implementadas

| Funcionalidad | Estado | Linea | Evidencia |
|---------------|:------:|:-----:|-----------|
| Abrir con evento | ✅ | 36 | `input<boolean>()` |
| Cerrar con X | ✅ | 130-132 | `onCloseClick()` |
| Cerrar con ESC | ✅ | 97-100 | `event.key === 'Escape'` |
| Cerrar con overlay | ✅ | 90-94 | `onOverlayClick()` |
| Bloqueo scroll | ✅ | 68, 79 | `body.style.overflow` |
| Focus trap | ✅ | 104-127 | Tab/Shift+Tab |
| Restore focus | ✅ | 65, 82-85 | `previouslyFocusedElement` |
| Usa `<dialog>` nativo | ✅ | 56 | `HTMLDialogElement` |

#### Evidencia de Codigo

**Archivo:** [`src/app/components/shared/modal/modal.ts`](../../src/app/components/shared/modal/modal.ts)

```typescript
// Lineas 63-87 - Effect con todas las funcionalidades
effect(() => {
  const dialog = this.dialogRef()?.nativeElement;
  if (!dialog) return;

  if (this.isOpen()) {
    // Guardar elemento con foco anterior
    this.previouslyFocusedElement = this.document.activeElement as HTMLElement;

    dialog.showModal();
    this.document.body.style.overflow = 'hidden';  // Bloqueo scroll

    // Focus al primer elemento focusable
    requestAnimationFrame(() => {
      const firstFocusable = dialog.querySelector<HTMLElement>(FOCUSABLE_SELECTOR);
      if (firstFocusable) {
        firstFocusable.focus();
      }
    });
  } else {
    dialog.close();
    this.document.body.style.overflow = '';

    // Restaurar foco
    if (this.previouslyFocusedElement) {
      this.previouslyFocusedElement.focus();
      this.previouslyFocusedElement = null;
    }
  }
});

// Lineas 103-127 - Focus trap
if (event.key === 'Tab') {
  if (event.shiftKey) {
    if (this.document.activeElement === firstElement) {
      event.preventDefault();
      lastElement.focus();
    }
  } else {
    if (this.document.activeElement === lastElement) {
      event.preventDefault();
      firstElement.focus();
    }
  }
}
```

#### Justificacion de la Puntuacion

**10/10** porque:
- ✅ Todas las funcionalidades requeridas implementadas
- ✅ Usa `<dialog>` nativo (HTML5 semantico)
- ✅ Focus trap completo con Tab y Shift+Tab
- ✅ Restore focus al cerrar
- ✅ Bloqueo de scroll del body

---

### 3.3 Accordion

#### Puntuacion: **6-7/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa accordion | ❌ |
| 2-4 | Toggle basico | ❌ |
| 5-7 | HTML5 nativo con iconos | ✅ |
| 8-9 | Custom con navegacion teclado | ❌ |
| 10 | Custom con animacion + solo 1 abierto | ❌ |

#### Funcionalidades Implementadas

| Funcionalidad | Estado | Nota |
|---------------|:------:|------|
| Expandir/colapsar click | ✅ | Nativo `<details>` |
| Enter/Space | ✅ | Nativo `<summary>` |
| Iconos indicadores | ✅ | Implementado |
| Arrow keys | ❌ | No nativo |
| Home/End | ❌ | No nativo |
| Solo 1 abierto | ❌ | No implementado |
| Animacion smooth | ❌ | No implementado |

#### Evidencia de Codigo

**Documentado en:** [`DOCUMENTACION.md`](./DOCUMENTACION.md) (lineas 271-299)

```html
<details class="accordion-item">
  <summary class="accordion-header">
    <ng-icon name="matCode" size="20" />
    <span>ViewChild y ElementRef</span>
  </summary>
  <div class="accordion-content">
    <p>Contenido explicativo...</p>
  </div>
</details>
```

#### Justificacion de la Puntuacion

**6-7/10** porque:
- ✅ Usa `<details>`/`<summary>` HTML5 (accesible por defecto)
- ✅ Iconos indicadores de estado
- ✅ Enter/Space funcionan nativamente
- ❌ Sin navegacion por Arrow keys
- ❌ Sin modo "solo 1 abierto"
- ❌ Sin animacion smooth

---

### 3.4 Tabs

#### Puntuacion: **9/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa tabs | ❌ |
| 2-4 | Tabs basicos sin ARIA | ❌ |
| 5-7 | Tabs con ARIA basico | ❌ |
| 8-9 | Tabs con ARIA completo | ✅ |
| 10 | Anterior + Arrow keys + Home/End | ❌ |

#### Funcionalidades Implementadas

| Funcionalidad | Estado | Evidencia |
|---------------|:------:|-----------|
| Cambio con click | ✅ | `selectedTab.set()` |
| `role="tablist"` | ✅ | Template HTML |
| `role="tab"` | ✅ | Template HTML |
| `aria-selected` | ✅ | Binding dinamico |
| `aria-controls` | ✅ | Template HTML |
| `role="tabpanel"` | ✅ | Template HTML |
| Arrow keys | ⚠️ | Via @angular/aria |

#### Evidencia de Codigo

**Archivo:** [`src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts`](../../src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts)

```typescript
// Linea 41
protected readonly selectedTab = signal('eventos');
```

**Documentado en:** [`DOCUMENTACION.md`](./DOCUMENTACION.md) (lineas 165-216)

```html
<div ngTabs>
  <ul ngTabList [(selectedTab)]="selectedTab" role="tablist">
    <li ngTab value="eventos" role="tab"
        [attr.aria-selected]="selectedTab() === 'eventos'">
      Eventos DOM
    </li>
    <li ngTab value="viewchild" role="tab"
        [attr.aria-selected]="selectedTab() === 'viewchild'">
      ViewChild
    </li>
  </ul>
  <div ngTabPanel value="eventos" role="tabpanel">
    <ng-template ngTabContent>...</ng-template>
  </div>
</div>
```

#### Justificacion de la Puntuacion

**9/10** porque:
- ✅ ARIA completo (tablist, tab, tabpanel, aria-selected, aria-controls)
- ✅ Signal-based state management
- ✅ Documentacion de navegacion por teclado
- ⚠️ Navegacion Arrow keys via libreria externa

---

### 3.5 Tooltip

#### Puntuacion: **9/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa tooltip | ❌ |
| 2-4 | Tooltip basico mouseenter | ❌ |
| 5-7 | Anterior + focus + delay | ❌ |
| 8-9 | Anterior + posicionamiento + ESC | ✅ |
| 10 | Anterior + flecha + ajuste viewport | ❌ |

#### Funcionalidades Implementadas

| Funcionalidad | Estado | Linea |
|---------------|:------:|:-----:|
| Mostrar mouseenter | ✅ | 51-52 |
| Ocultar mouseleave | ✅ | 59-60 |
| Mostrar focus | ✅ | 52 |
| Ocultar blur | ✅ | 60 |
| Delay configurable | ✅ | 43 |
| Posicionamiento (top/bottom/left/right) | ✅ | 143-160 |
| Cierre con ESC | ✅ | 69-72 |
| aria-describedby | ✅ | 114 |
| Animacion fade-in | ✅ | 117-121 |
| Ajuste viewport | ✅ | 166-173 |
| **Flecha indicadora** | ❌ | - |

#### Evidencia de Codigo

**Archivo:** [`src/app/directives/tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts)

```typescript
// Linea 43 - Delay configurable
readonly tooltipDelay = input<number>(200);

// Lineas 143-160 - Posicionamiento dinamico
switch (this.tooltipPosition()) {
  case 'top':
    top = hostRect.top + scrollTop - tooltipRect.height - TOOLTIP_OFFSET;
    left = hostRect.left + scrollLeft + (hostRect.width - tooltipRect.width) / 2;
    break;
  case 'bottom':
    top = hostRect.bottom + scrollTop + TOOLTIP_OFFSET;
    left = hostRect.left + scrollLeft + (hostRect.width - tooltipRect.width) / 2;
    break;
  // ... left, right
}

// Lineas 166-173 - Ajuste viewport
if (left < 0) left = TOOLTIP_OFFSET;
if (left + tooltipRect.width > viewportWidth) {
  left = viewportWidth - tooltipRect.width - TOOLTIP_OFFSET;
}
if (top < 0) top = TOOLTIP_OFFSET;
```

#### Justificacion de la Puntuacion

**9/10** porque:
- ✅ Todas las funcionalidades excepto flecha
- ✅ Posicionamiento 4 direcciones
- ✅ Ajuste automatico al viewport
- ✅ Accesibilidad con aria-describedby
- ❌ Falta flecha indicadora CSS

---

## CE6.g: DOCUMENTACION TECNICA

### Puntuacion Total: **18-19/30**

---

### 5.1 Seccion de Arquitectura de Eventos

#### Puntuacion: **9-10/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No existe documentacion | ❌ |
| 2-4 | Documentacion basica <300 palabras | ❌ |
| 5-7 | 300-500 palabras con ejemplos | ❌ |
| 8-9 | 500+ palabras, estructura clara | ⚠️ |
| **10** | **Documentacion profesional completa** | ✅ |

#### Contenido Documentado

**Archivo:** [`DOCUMENTACION.md`](./DOCUMENTACION.md) (1246+ lineas)

| Seccion | Lineas | Contenido |
|---------|:------:|-----------|
| 1.1 ViewChild/ElementRef | 13-62 | Explicacion y ejemplos |
| 1.2 Event Binding | 63-113 | Todos los eventos |
| 1.3 preventDefault/stopPropagation | 115-161 | Control de eventos |
| 1.4 Componentes Interactivos | 163-299 | Tabs, Tooltip, Accordion |
| 2.1 Arquitectura Servicios | 302-353 | Diagrama ASCII |
| 2.4 EventBusService | 563-668 | Patron Observer |

#### Verificacion de Requisitos

| Requisito | Cumple |
|-----------|:------:|
| Minimo 500 palabras | ✅ (1246+ lineas) |
| Patron de manejo de eventos | ✅ |
| Tipos de event binding | ✅ |
| Uso de @HostListener | ✅ |
| Manipulacion del DOM | ✅ |
| Codigo con sintaxis correcta | ✅ |
| Estructura con subtitulos | ✅ |
| Tablas comparativas | ✅ |
| Buenas practicas | ✅ |

#### Justificacion de la Puntuacion

**9-10/10** porque:
- ✅ Documentacion extensa (1246+ lineas)
- ✅ Estructura profesional con indice
- ✅ Ejemplos de codigo ejecutables
- ✅ Tablas comparativas
- ✅ Diagramas ASCII

---

### 5.2 Diagrama de Flujo de Eventos

#### Puntuacion: **9/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No existe diagrama | ❌ |
| 2-4 | Diagrama basico | ❌ |
| 5-7 | Diagrama con flujo claro | ❌ |
| 8-9 | Diagrama profesional ASCII/SVG | ✅ |
| 10 | Multiples diagramas interconectados | ❌ |

#### Diagramas Incluidos

**1. Arquitectura de Servicios** ([DOCUMENTACION.md](./DOCUMENTACION.md) lineas 310-344):

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CAPA DE PRESENTACION                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │
│  │ Componente  │  │ Componente  │  │ Componente  │                 │
│  │     A       │  │     B       │  │     C       │                 │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                 │
└─────────┼───────────────────┼───────────────┼───────────────────────┘
          │                   │               │
          ▼                   ▼               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         CAPA DE SERVICIOS                           │
│  ┌───────────────────┐  ┌───────────────────┐  ┌─────────────────┐ │
│  │ NotificationService│  │   LoadingService  │  │  EventBusService│ │
│  └───────────────────┘  └───────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

**2. Flujo EventBus** ([DOCUMENTACION.md](./DOCUMENTACION.md) lineas 612-638):

```
┌─────────────────┐                         ┌─────────────────┐
│  Componente A   │                         │  Componente B   │
│    (Emisor)     │                         │   (Receptor)    │
└────────┬────────┘                         └────────┬────────┘
         │  emit('user-selected', user)              │
         ▼                                           │
┌────────────────────────────────────────────────────┴────────┐
│                      EventBusService                        │
│  Subject<BusEvent>                                          │
│  name: 'user-selected'                                      │
│  data: { id: 1, name: 'Juan' }                              │
└────────────────────────────────────────────────────────────┬┘
                                                             │
         onSignal('user-selected') → Signal<User>            │
                                                             ▼
                                              ┌─────────────────┐
                                              │  Componente B   │
                                              │  Recibe datos   │
                                              └─────────────────┘
```

#### Justificacion de la Puntuacion

**9/10** porque:
- ✅ Diagramas ASCII profesionales
- ✅ Flujo de datos claro
- ✅ Arquitectura por capas
- ⚠️ Podria tener mas diagramas de flujo de eventos especificos

---

### 5.3 Tabla de Compatibilidad de Navegadores

#### Puntuacion: **0/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| **0** | **No existe tabla** | ✅ |
| 2-4 | Tabla basica sin versiones | ❌ |
| 5-7 | Tabla con versiones principales | ❌ |
| 8-9 | Tabla completa con notas | ❌ |
| 10 | Tabla + polyfills + fallbacks | ❌ |

#### Busqueda Realizada

- ✅ Revisado DOCUMENTACION.md completo (1246 lineas)
- ❌ No existe tabla con columnas Chrome, Firefox, Safari, Edge
- ❌ No hay informacion de versiones de navegadores
- ❌ No hay simbolos de soporte (✓/✗)

#### Requisitos NO Cumplidos

| Requisito | Estado |
|-----------|:------:|
| Lista de eventos implementados | ❌ |
| Columnas Chrome/Firefox/Safari/Edge | ❌ |
| Versiones minimas soportadas | ❌ |
| Simbolos de soporte | ❌ |
| Minimo 8 eventos | ❌ |
| Notas sobre fallbacks/polyfills | ❌ |

#### Justificacion de la Puntuacion

**0/10** porque:
- ❌ La tabla de compatibilidad de navegadores NO EXISTE en la documentacion
- Este criterio requiere documentar la compatibilidad de cada evento con diferentes navegadores

---

## CE6.h: THEME SWITCHER

### Puntuacion: **10/10**

#### Requisitos de la Rubrica

| Puntos | Requisito | Cumple |
|:------:|-----------|:------:|
| 0 | No implementa theme switcher | ❌ |
| 2-4 | Toggle basico sin persistencia | ❌ |
| 5-7 | Toggle + localStorage | ❌ |
| 8-9 | Anterior + prefers-color-scheme | ❌ |
| **10** | **Completo con todo lo anterior + cross-tab** | ✅ |

#### Funcionalidades Implementadas

| Funcionalidad | Estado | Linea |
|---------------|:------:|:-----:|
| Detecta `prefers-color-scheme` | ✅ | 108 |
| `matchMedia.addEventListener('change')` | ✅ | 112 |
| Toggle claro/oscuro | ✅ | 80-83 |
| Persistencia localStorage | ✅ | 60 |
| Aplica al cargar | ✅ | 96-102 |
| CSS Custom Properties | ✅ | 67 |
| Signal para estado reactivo | ✅ | 31, 38 |
| Cross-tab sync | ✅ | 120-128 |
| SSR-safe | ✅ | 50 |

#### Evidencia de Codigo

**Archivo:** [`src/app/services/theme.service.ts`](../../src/app/services/theme.service.ts)

```typescript
// Lineas 107-114 - Deteccion prefers-color-scheme
private initializeSystemTheme(): void {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
  this.systemTheme.set(mediaQuery.matches ? 'dark' : 'light');

  // Escucha cambios en tiempo real
  mediaQuery.addEventListener('change', (event) => {
    this.systemTheme.set(event.matches ? 'dark' : 'light');
  });
}

// Lineas 80-83 - Toggle
toggleTheme(): void {
  const current = this.currentTheme();
  this.selectedTheme.set(current === 'light' ? 'dark' : 'light');
}

// Lineas 59-61 - Persistencia localStorage
effect(() => {
  localStorage.setItem('antipanel-theme', this.selectedTheme());
});

// Lineas 64-71 - Aplicar al DOM
effect(() => {
  const theme = this.currentTheme();
  if (theme === 'light') {
    document.documentElement.setAttribute('data-theme', 'light');
  } else {
    document.documentElement.removeAttribute('data-theme');
  }
});

// Lineas 120-128 - Cross-tab sync
private setupStorageListener(): void {
  window.addEventListener('storage', (event) => {
    if (event.key === 'antipanel-theme' && event.newValue) {
      const newTheme = event.newValue as Theme;
      if (['light', 'dark', 'system'].includes(newTheme)) {
        this.selectedTheme.set(newTheme);
      }
    }
  });
}
```

#### Justificacion de la Puntuacion

**10/10** porque:
- ✅ Detecta preferencia del sistema con `matchMedia`
- ✅ Escucha cambios en tiempo real con `addEventListener('change')`
- ✅ Toggle funcional
- ✅ Persistencia en localStorage
- ✅ Cross-tab synchronization con evento `storage`
- ✅ SSR-safe con `isPlatformBrowser`
- ✅ Usa Angular 21 Signals

---

## ARCHIVOS DE REFERENCIA

### Componentes Principales

| Funcionalidad | Ruta | Descripcion |
|---------------|------|-------------|
| ViewChild/ElementRef | [`src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts`](../../src/app/pages/cliente/sections/dom-events-section/dom-events-section.ts) | Demos de manipulacion DOM |
| Renderer2 | [`src/app/directives/tooltip.directive.ts`](../../src/app/directives/tooltip.directive.ts) | Directiva con 14+ usos Renderer2 |
| Modal | [`src/app/components/shared/modal/modal.ts`](../../src/app/components/shared/modal/modal.ts) | Modal accesible completo |
| Header/Menu | [`src/app/components/layout/header/header.ts`](../../src/app/components/layout/header/header.ts) | Menu hamburguesa + dropdown |
| Auth Form | [`src/app/components/shared/auth-form/auth-form.ts`](../../src/app/components/shared/auth-form/auth-form.ts) | Formulario reactivo |
| Order Ready | [`src/app/components/shared/order-ready/order-ready.ts`](../../src/app/components/shared/order-ready/order-ready.ts) | Edicion inline |

### Servicios

| Servicio | Ruta | Descripcion |
|----------|------|-------------|
| ThemeService | [`src/app/services/theme.service.ts`](../../src/app/services/theme.service.ts) | Gestion de tema |
| NotificationService | [`src/app/services/notification.service.ts`](../../src/app/services/notification.service.ts) | Notificaciones toast |
| LoadingService | [`src/app/services/loading.service.ts`](../../src/app/services/loading.service.ts) | Estado de carga |
| EventBusService | [`src/app/services/event-bus.service.ts`](../../src/app/services/event-bus.service.ts) | Comunicacion componentes |

### Validadores

| Validador | Ruta |
|-----------|------|
| Password Strength | [`src/app/core/validators/sync/password-strength.validator.ts`](../../src/app/core/validators/sync/password-strength.validator.ts) |
| Password Match | [`src/app/core/validators/sync/password-match.validator.ts`](../../src/app/core/validators/sync/password-match.validator.ts) |
| Email Unique | [`src/app/core/validators/async/email-unique.validator.ts`](../../src/app/core/validators/async/email-unique.validator.ts) |

### Documentacion

| Documento | Ruta | Contenido |
|-----------|------|-----------|
| Documentacion Principal | [`docs/client/DOCUMENTACION.md`](./DOCUMENTACION.md) | 1246+ lineas |
| Este documento | [`docs/client/justificacion_ra_fase1_2_y_3.md`](./justificacion_ra_fase1_2_y_3.md) | Justificacion por RA |

---

## DECISIONES ARQUITECTONICAS ANGULAR 21

Esta seccion explica por que ciertas implementaciones se mantienen como estan, siguiendo las **mejores practicas de Angular 21** en lugar de implementar soluciones que serian over-engineering.

---

### Por que Renderer2 solo en TooltipDirective

**Decision:** No extender Renderer2 a modal.ts y theme.service.ts

**Justificacion tecnica:**

| Componente | Implementacion Actual | Por que es CORRECTO |
|------------|----------------------|---------------------|
| `modal.ts` | `inject(DOCUMENT)` | SSR-safe, usa `<dialog>` nativo HTML5 |
| `theme.service.ts` | `isPlatformBrowser()` + `document.documentElement` | Operacion global donde Renderer2 no aporta beneficio |
| `tooltip.directive.ts` | `inject(Renderer2)` | Crea elementos dinamicos fuera del host - USO CORRECTO |

**Referencia Angular 21:**
- Renderer2 esta disenado para manipulacion de elementos **dentro del contexto del componente**
- Para operaciones globales (document.body, document.documentElement), el acceso directo con proteccion SSR es la practica recomendada
- Forzar Renderer2 en todos los lugares seria **over-engineering** sin beneficio real

---

### Por que Accordion usa HTML5 nativo

**Decision:** Usar `<details>/<summary>` en lugar de componente Angular custom

**Justificacion tecnica:**

| Aspecto | HTML5 Nativo | Componente Custom |
|---------|--------------|-------------------|
| Accesibilidad | ✅ Perfecta por defecto | Requiere implementacion manual |
| Navegacion teclado | ✅ Enter/Space built-in | Hay que implementar |
| JavaScript requerido | ✅ 0kb | Varios KB adicionales |
| Mantenibilidad | ✅ Sin codigo que mantener | Codigo adicional |
| Soporte navegadores | ✅ 98%+ | Depende de implementacion |

**Referencia Angular 21:**
- Angular 21 promueve el uso de **APIs nativas del navegador** cuando son suficientes
- El patron "JavaScript-less" reduce bundle size y mejora rendimiento
- `<details>/<summary>` cumple con WCAG 2.1 sin codigo adicional

---

### Por que no tabla de compatibilidad de navegadores

**Decision:** No incluir tabla de compatibilidad en documentacion

**Justificacion tecnica:**

1. **Angular 21 define navegadores oficialmente:**
   - Chrome (ultimas 2 versiones)
   - Firefox (ultimas 2 versiones)
   - Edge (ultimas 2 versiones)
   - Safari (ultimas 2 versiones)

2. **Features usadas tienen soporte universal:**
   - `<details>/<summary>`: 98%+
   - `<dialog>`: 96%+
   - Signals: Compilados por Angular, no dependen del navegador
   - CSS Custom Properties: 97%+

3. **El framework garantiza compatibilidad:**
   - Angular CLI configura browserslist automaticamente
   - Polyfills incluidos en el build

**Conclusion:** La tabla seria informacion redundante que Angular 21 ya maneja automaticamente.

---

## CONCLUSION

### Resumen de Puntuaciones

| CE | Descripcion | Puntos |
|:--:|-------------|:------:|
| CE6.a | Separacion contenido/aspecto/comportamiento | **10/10** |
| CE6.c | Manipulacion DOM (ViewChild, Renderer2) | **24-25/30** |
| CE6.d | Sistema de eventos | **37-38/40** |
| CE6.e | Componentes interactivos | **43-45/50** |
| CE6.g | Documentacion tecnica | **18-19/30** |
| CE6.h | Theme Switcher | **10/10** |
| **TOTAL** | | **143-148/170** |

### Porcentaje Final: **84-87%**

### Principales Fortalezas

1. **Theme Switcher 100% completo** - Incluye cross-tab sync y deteccion de sistema
2. **Modal profesional** - Focus trap, restore focus, scroll lock, ESC, overlay
3. **Sistema de eventos robusto** - 11+ tipos de eventos, navegacion WAI-ARIA
4. **Menu hamburguesa accesible** - Toggle, click fuera, ESC, aria-expanded
5. **Separacion de responsabilidades perfecta** - Arquitectura limpia
6. **Documentacion extensa** - 1246+ lineas con diagramas ASCII
7. **Decisiones arquitectonicas justificadas** - Siguiendo mejores practicas Angular 21

### Areas de Mejora Identificadas

1. **Tabla de navegadores** - No incluida (ver [Decisiones Arquitectonicas](#por-que-no-tabla-de-compatibilidad-de-navegadores))
2. **Renderer2** - Concentrado en TooltipDirective (ver [Decisiones Arquitectonicas](#por-que-renderer2-solo-en-tooltipdirective))
3. **Accordion** - Usa HTML5 nativo (ver [Decisiones Arquitectonicas](#por-que-accordion-usa-html5-nativo))

---

**Documento generado:** Enero 2026
**Framework:** Angular 21
**Autor:** Estudiante
