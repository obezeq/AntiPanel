# Documentacion del Desarrollo Frontend - Fases 1, 2 y 3

## Introduccion

En este documento detallo la implementacion completa de las tres fases del proyecto AntiPanel utilizando Angular 21. He decidido seguir las mejores practicas actuales del framework, incluyendo signals para gestion de estado, zoneless change detection, y la libreria `@angular/aria` para accesibilidad.

Toda la funcionalidad se encuentra centralizada en la ruta `/cliente`, organizada en tres secciones que corresponden a cada fase del desarrollo.

---

## FASE 1: Manipulacion del DOM y Eventos

### 1.1 ViewChild y ElementRef

En Angular 21, he utilizado la funcion `viewChild()` que devuelve un signal en lugar del decorador clasico `@ViewChild`. Esto proporciona mejor integracion con el sistema de reactividad moderno.

**Implementacion:**

```typescript
import { viewChild, ElementRef } from '@angular/core';

export class MiComponente {
  // Referencia al elemento del template
  protected readonly demoInput = viewChild<ElementRef<HTMLInputElement>>('demoInput');
  protected readonly demoBox = viewChild<ElementRef<HTMLDivElement>>('demoBox');

  // Focus programatico
  protected focusInput(): void {
    const input = this.demoInput();
    if (input) {
      input.nativeElement.focus();
    }
  }

  // Lectura de contenido
  protected getBoxText(): void {
    const box = this.demoBox();
    if (box) {
      console.log('Texto:', box.nativeElement.innerText);
    }
  }
}
```

**Template:**

```html
<input #demoInput type="text" />
<div #demoBox>Contenido manipulable</div>
<button (click)="focusInput()">Enfocar Input</button>
<button (click)="getBoxText()">Leer Texto</button>
```

**Diferencia con el enfoque clasico:**

| Aspecto | @ViewChild (clasico) | viewChild() (Angular 21) |
|---------|---------------------|--------------------------|
| Sintaxis | `@ViewChild('ref') elemento!: ElementRef` | `readonly elemento = viewChild<ElementRef>('ref')` |
| Tipo de retorno | Propiedad directa | Signal |
| Disponibilidad | ngAfterViewInit | Inmediata (via signal) |
| Tipado | Requiere assertion | Inferencia automatica |

### 1.2 Event Binding

He implementado binding de eventos para todas las interacciones comunes del usuario. Angular utiliza parentesis `(evento)` para vincular eventos del DOM.

**Eventos implementados:**

```html
<!-- Click -->
<button (click)="onButtonClick()">Haz click</button>

<!-- Keydown con acceso al evento -->
<input (keydown)="onKeyDown($event)" />

<!-- Focus y Blur -->
<input (focus)="onFocus()" (blur)="onBlur()" />

<!-- Mousemove con coordenadas -->
<div (mousemove)="onMouseMove($event)">Area de seguimiento</div>

<!-- Keydown especifico -->
<input (keydown.enter)="onEnter()" (keydown.escape)="onCancel()" />
```

**Handlers en el componente:**

```typescript
protected readonly clickCount = signal(0);
protected readonly lastKey = signal('');
protected readonly hasFocus = signal(false);
protected readonly mousePosition = signal({ x: 0, y: 0 });

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

### 1.3 preventDefault y stopPropagation

Estos dos metodos son fundamentales para controlar el comportamiento de eventos en el DOM.

**preventDefault()** - Cancela el comportamiento por defecto del evento:

```typescript
// Evitar que un link navegue
onLinkClick(event: MouseEvent): void {
  event.preventDefault();
  // Ejecutar logica personalizada en lugar de navegar
  this.processLink();
}

// Evitar que un formulario recargue la pagina
onFormSubmit(event: SubmitEvent): void {
  event.preventDefault();
  // Enviar datos via AJAX en lugar de submit tradicional
  this.submitFormAsync();
}
```

**stopPropagation()** - Detiene la propagacion del evento hacia elementos padre (event bubbling):

```typescript
onOuterClick(): void {
  console.log('Click en contenedor exterior');
}

onInnerClick(event: MouseEvent): void {
  event.stopPropagation(); // El click NO llega al contenedor exterior
  console.log('Click solo en elemento interior');
}
```

**Ejemplo en template:**

```html
<div class="contenedor" (click)="onOuterClick()">
  <button (click)="onInnerClick($event)">
    Con stopPropagation (no propaga)
  </button>
  <button (click)="onInnerClickNormal($event)">
    Sin stopPropagation (propaga al padre)
  </button>
</div>
```

### 1.4 Componentes Interactivos

#### 1.4.1 Tabs con @angular/aria

He utilizado la libreria oficial `@angular/aria` para crear tabs completamente accesibles sin necesidad de implementar manualmente la logica de accesibilidad.

**Instalacion:**

```bash
bun add @angular/aria @angular/cdk
```

**Implementacion:**

```typescript
import { Tabs, TabList, Tab, TabPanel, TabContent } from '@angular/aria/tabs';

@Component({
  imports: [Tabs, TabList, Tab, TabPanel, TabContent]
})
export class MiComponente {
  protected readonly selectedTab = signal('tab1');
}
```

```html
<div ngTabs>
  <ul ngTabList [(selectedTab)]="selectedTab">
    <li ngTab value="eventos">Eventos DOM</li>
    <li ngTab value="viewchild">ViewChild</li>
    <li ngTab value="tooltips">Tooltips</li>
  </ul>

  <div ngTabPanel value="eventos">
    <ng-template ngTabContent>
      <!-- Contenido lazy-loaded -->
      <p>Contenido del tab de eventos</p>
    </ng-template>
  </div>

  <div ngTabPanel value="viewchild">
    <ng-template ngTabContent>
      <p>Contenido del tab de ViewChild</p>
    </ng-template>
  </div>
</div>
```

**Caracteristicas de accesibilidad automaticas:**
- Navegacion por teclado (flechas, Home, End, Tab)
- Roles ARIA gestionados automaticamente
- `aria-selected`, `aria-controls`, `aria-labelledby`
- Lazy loading del contenido con `ngTabContent`

#### 1.4.2 Tooltip Directive

He creado una directiva personalizada para mostrar tooltips accesibles.

**Ubicacion:** `directives/tooltip.directive.ts`

```typescript
@Directive({
  selector: '[appTooltip]',
  standalone: true
})
export class TooltipDirective {
  readonly appTooltip = input.required<string>();
  readonly tooltipPosition = input<TooltipPosition>('top');
  readonly tooltipDelay = input<number>(200);

  @HostListener('mouseenter')
  @HostListener('focus')
  onShowTooltip(): void {
    setTimeout(() => this.show(), this.tooltipDelay());
  }

  @HostListener('mouseleave')
  @HostListener('blur')
  onHideTooltip(): void {
    this.hide();
  }

  @HostListener('keydown.escape')
  onEscape(): void {
    this.hide();
  }
}
```

**Uso:**

```html
<button appTooltip="Tooltip arriba" tooltipPosition="top">Arriba</button>
<button appTooltip="Tooltip abajo" tooltipPosition="bottom">Abajo</button>
<button appTooltip="Tooltip izquierda" tooltipPosition="left">Izquierda</button>
<button appTooltip="Tooltip derecha" tooltipPosition="right">Derecha</button>
```

**Caracteristicas de accesibilidad:**
- Muestra en hover Y focus (accesible por teclado)
- Cierre con tecla Escape
- `aria-describedby` automatico
- Posicionamiento inteligente que evita salirse del viewport

#### 1.4.3 Accordion Nativo

Para el accordion he optado por usar los elementos HTML5 nativos `<details>` y `<summary>`, que proporcionan la mejor accesibilidad posible sin JavaScript adicional.

```html
<details class="accordion-item">
  <summary class="accordion-header">
    <ng-icon name="matCode" size="20" />
    <span>ViewChild y ElementRef</span>
  </summary>
  <div class="accordion-content">
    <p>Contenido explicativo sobre ViewChild...</p>
    <pre><code>// Ejemplo de codigo</code></pre>
  </div>
</details>

<details class="accordion-item">
  <summary class="accordion-header">
    <ng-icon name="matTouchApp" size="20" />
    <span>Event Binding</span>
  </summary>
  <div class="accordion-content">
    <p>Informacion sobre event binding...</p>
  </div>
</details>
```

**Ventajas de usar elementos nativos:**
- Accesibilidad perfecta sin codigo adicional
- Funciona sin JavaScript
- Soporte nativo de teclado (Enter/Space para toggle)
- Estado abierto/cerrado via atributo `[open]`

---

## FASE 2: Servicios y Comunicacion

### 2.1 Arquitectura de Servicios

He diseñado una arquitectura de servicios que sigue el principio de separacion de responsabilidades. Los servicios gestionan la logica y el estado, mientras que los componentes se encargan exclusivamente de la presentacion.

**Diagrama de Arquitectura:**

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CAPA DE PRESENTACION                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │
│  │ Componente  │  │ Componente  │  │ Componente  │                 │
│  │     A       │  │     B       │  │     C       │                 │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                 │
│         │                │                │                         │
└─────────┼────────────────┼────────────────┼─────────────────────────┘
          │                │                │
          ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         CAPA DE SERVICIOS                           │
│                                                                     │
│  ┌───────────────────┐  ┌───────────────────┐  ┌─────────────────┐ │
│  │ NotificationService│  │   LoadingService  │  │  EventBusService│ │
│  │                   │  │                   │  │                 │ │
│  │ - notifications   │  │ - isLoading       │  │ - emit()        │ │
│  │ - success()       │  │ - activeRequests  │  │ - on()          │ │
│  │ - error()         │  │ - show()          │  │ - onSignal()    │ │
│  │ - warning()       │  │ - hide()          │  │                 │ │
│  │ - info()          │  │                   │  │                 │ │
│  └───────────────────┘  └───────────────────┘  └─────────────────┘ │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
          │                │                │
          ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CAPA DE INFRAESTRUCTURA                        │
│  ┌───────────────────┐  ┌───────────────────────────────────────┐  │
│  │   HTTP Interceptor │  │            Estado Global              │  │
│  │   (auto-loading)   │  │       (Signals reactivos)             │  │
│  └───────────────────┘  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

**Principio de Separacion de Responsabilidades:**

| Capa | Responsabilidad | Ejemplo |
|------|-----------------|---------|
| Componente | Solo presentacion y binding | Mostrar lista, manejar clicks |
| Servicio | Logica de negocio y estado | Validar datos, transformar, almacenar |
| Interceptor | Logica transversal | Auto-loading en peticiones HTTP |

### 2.2 NotificationService

He implementado un servicio centralizado para gestionar notificaciones toast en toda la aplicacion.

**Ubicacion:** `services/notification.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly _notifications = signal<Notification[]>([]);

  // Signals publicos (solo lectura)
  readonly notifications = this._notifications.asReadonly();
  readonly count = computed(() => this._notifications().length);
  readonly hasNotifications = computed(() => this._notifications().length > 0);

  /**
   * Muestra una notificacion de exito
   * @param message - Mensaje a mostrar
   * @param options - Opciones adicionales (titulo, duracion)
   */
  success(message: string, options?: NotificationOptions): string {
    return this.addNotification('success', message, options);
  }

  /**
   * Muestra una notificacion de error
   */
  error(message: string, options?: NotificationOptions): string {
    return this.addNotification('error', message, options);
  }

  /**
   * Muestra una notificacion de advertencia
   */
  warning(message: string, options?: NotificationOptions): string {
    return this.addNotification('warning', message, options);
  }

  /**
   * Muestra una notificacion informativa
   */
  info(message: string, options?: NotificationOptions): string {
    return this.addNotification('info', message, options);
  }

  /**
   * Cierra una notificacion especifica
   */
  dismiss(id: string): void {
    this._notifications.update(list =>
      list.filter(n => n.id !== id)
    );
  }

  /**
   * Cierra todas las notificaciones
   */
  dismissAll(): void {
    this._notifications.set([]);
  }
}
```

**Tipos de notificacion:**

| Tipo | Color | Uso |
|------|-------|-----|
| success | Verde | Operaciones completadas correctamente |
| error | Rojo | Errores y fallos |
| warning | Amarillo | Advertencias que requieren atencion |
| info | Azul | Informacion general |

**Auto-dismiss configurable:**

```typescript
// Duracion por defecto: 5000ms
this.notificationService.success('Guardado correctamente');

// Duracion personalizada: 10000ms
this.notificationService.warning('Atencion requerida', {
  title: 'Advertencia',
  duration: 10000
});

// Sin auto-dismiss (debe cerrarse manualmente)
this.notificationService.error('Error critico', {
  duration: 0
});
```

**Uso en componentes:**

```typescript
export class MiComponente {
  private readonly notificationService = inject(NotificationService);

  async guardarDatos(): Promise<void> {
    try {
      await this.apiService.save(this.datos);
      this.notificationService.success('Datos guardados correctamente');
    } catch (error) {
      this.notificationService.error('Error al guardar los datos');
    }
  }
}
```

### 2.3 LoadingService

He creado un servicio para gestionar estados de carga tanto globales como locales.

**Ubicacion:** `services/loading.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly _activeRequests = signal(0);

  // Estado global de carga
  readonly isLoading = computed(() => this._activeRequests() > 0);
  readonly activeRequests = this._activeRequests.asReadonly();

  /**
   * Incrementa el contador de requests activos
   */
  show(): void {
    this._activeRequests.update(count => count + 1);
  }

  /**
   * Decrementa el contador de requests activos
   */
  hide(): void {
    this._activeRequests.update(count => Math.max(0, count - 1));
  }

  /**
   * Ejecuta una funcion async mostrando loading automaticamente
   */
  async withLoading<T>(fn: () => Promise<T>): Promise<T> {
    this.show();
    try {
      return await fn();
    } finally {
      this.hide();
    }
  }
}
```

**HTTP Interceptor para loading automatico:**

```typescript
// core/interceptors/loading.interceptor.ts
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  const loadingService = inject(LoadingService);

  // Ignorar ciertas URLs (assets, etc.)
  if (shouldIgnoreUrl(req.url)) {
    return next(req);
  }

  loadingService.show();

  return next(req).pipe(
    finalize(() => loadingService.hide())
  );
};
```

**Loading global (spinner overlay):**

```html
<!-- En app.component.html -->
@if (loadingService.isLoading()) {
  <div class="loading-overlay">
    <app-spinner size="lg" />
  </div>
}
```

**Loading local en botones:**

```html
<button [disabled]="isSubmitting()" (click)="submit()">
  @if (isSubmitting()) {
    <app-spinner size="sm" />
  } @else {
    <ng-icon name="matSave" />
  }
  <span>Guardar</span>
</button>
```

```typescript
protected readonly isSubmitting = signal(false);

async submit(): Promise<void> {
  this.isSubmitting.set(true);
  try {
    await this.service.save(this.data);
    this.notificationService.success('Guardado');
  } finally {
    this.isSubmitting.set(false);
  }
}
```

### 2.4 EventBusService

He implementado un servicio de comunicacion pub/sub para permitir la comunicacion entre componentes hermanos (sin relacion padre-hijo).

**Ubicacion:** `services/event-bus.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class EventBusService {
  private readonly eventSubject = new Subject<BusEvent>();
  private readonly _history = signal<BusEvent[]>([]);

  readonly historyCount = computed(() => this._history().length);

  /**
   * Emite un evento al bus
   */
  emit<T>(name: string, data: T): void {
    const event: BusEvent = { name, data, timestamp: Date.now() };
    this._history.update(h => [...h, event]);
    this.eventSubject.next(event);
  }

  /**
   * Suscribirse a un evento (retorna Observable)
   */
  on<T>(name: string): Observable<T> {
    return this.eventSubject.asObservable().pipe(
      filter(event => event.name === name),
      map(event => event.data as T)
    );
  }

  /**
   * Suscribirse a un evento (retorna Signal)
   */
  onSignal<T>(name: string): Signal<T | undefined> {
    const observable = this.on<T>(name);
    return toSignal(observable);
  }

  clearHistory(): void {
    this._history.set([]);
  }
}
```

**Diagrama de flujo de comunicacion:**

```
┌─────────────────┐                         ┌─────────────────┐
│  Componente A   │                         │  Componente B   │
│    (Emisor)     │                         │   (Receptor)    │
└────────┬────────┘                         └────────┬────────┘
         │                                           │
         │  emit('user-selected', user)              │
         ▼                                           │
┌────────────────────────────────────────────────────┴────────┐
│                      EventBusService                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Subject<BusEvent>                                    │  │
│  │  ─────────────────────────────────────────────────────│  │
│  │  name: 'user-selected'                                │  │
│  │  data: { id: 1, name: 'Juan' }                        │  │
│  │  timestamp: 1702912345678                             │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┬┘
                                                             │
         onSignal('user-selected') → Signal<User>            │
                                                             ▼
                                              ┌─────────────────┐
                                              │  Componente B   │
                                              │  Recibe datos   │
                                              │  y actualiza UI │
                                              └─────────────────┘
```

**Uso - Componente Emisor:**

```typescript
export class ComponenteA {
  private readonly eventBus = inject(EventBusService);

  seleccionarUsuario(user: User): void {
    this.eventBus.emit('user-selected', user);
  }
}
```

**Uso - Componente Receptor:**

```typescript
export class ComponenteB {
  private readonly eventBus = inject(EventBusService);

  // Opcion 1: Signal (recomendado)
  protected readonly usuario = this.eventBus.onSignal<User>('user-selected');

  // Opcion 2: Observable (para logica compleja)
  constructor() {
    this.eventBus.on<User>('user-selected').subscribe(user => {
      console.log('Usuario recibido:', user);
    });
  }
}
```

### 2.5 Buenas Practicas de Separacion

He aplicado consistentemente el principio de que los servicios gestionan la logica mientras los componentes solo manejan la presentacion.

**Ejemplo de separacion correcta:**

```typescript
// ❌ MAL: Logica en el componente
@Component({...})
export class MalComponente {
  usuarios: User[] = [];

  async cargarUsuarios(): Promise<void> {
    const response = await fetch('/api/users');
    const data = await response.json();
    this.usuarios = data.filter(u => u.active).map(u => ({
      ...u,
      fullName: `${u.firstName} ${u.lastName}`
    }));
  }
}

// ✓ BIEN: Logica en el servicio
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);

  getActiveUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users').pipe(
      map(users => users.filter(u => u.active)),
      map(users => users.map(u => ({
        ...u,
        fullName: `${u.firstName} ${u.lastName}`
      })))
    );
  }
}

@Component({...})
export class BuenComponente {
  private readonly userService = inject(UserService);
  protected readonly usuarios = signal<User[]>([]);

  async cargarUsuarios(): Promise<void> {
    const users = await firstValueFrom(this.userService.getActiveUsers());
    this.usuarios.set(users);
  }
}
```

---

## FASE 3: Formularios Reactivos Avanzados

### 3.1 FormBuilder y ReactiveFormsModule

He utilizado `NonNullableFormBuilder` en lugar del `FormBuilder` estandar para obtener mejor inferencia de tipos y evitar valores null.

```typescript
import { NonNullableFormBuilder, Validators } from '@angular/forms';

@Component({...})
export class MiFormulario {
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, passwordStrengthValidator()]]
  });
}
```

### 3.2 Catalogo de Validadores

#### 3.2.1 Validadores Sincronos Integrados

Angular proporciona validadores listos para usar:

| Validador | Uso | Ejemplo |
|-----------|-----|---------|
| `Validators.required` | Campo obligatorio | `nombre: ['', Validators.required]` |
| `Validators.minLength(n)` | Longitud minima | `nombre: ['', Validators.minLength(2)]` |
| `Validators.maxLength(n)` | Longitud maxima | `bio: ['', Validators.maxLength(500)]` |
| `Validators.email` | Formato email | `email: ['', Validators.email]` |
| `Validators.pattern(regex)` | Patron personalizado | `codigo: ['', Validators.pattern(/^[A-Z]{3}$/)]` |
| `Validators.min(n)` | Valor minimo | `edad: [0, Validators.min(18)]` |
| `Validators.max(n)` | Valor maximo | `cantidad: [1, Validators.max(100)]` |

#### 3.2.2 Validador de Fortaleza de Contrasena

He implementado un validador configurable que verifica multiples requisitos de seguridad.

**Ubicacion:** `core/validators/sync/password-strength.validator.ts`

```typescript
export function passwordStrengthValidator(config?: PasswordStrengthConfig): ValidatorFn {
  const finalConfig = {
    minLength: 8,
    requireUppercase: true,
    requireLowercase: true,
    requireNumber: true,
    requireSpecial: true,
    ...config
  };

  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) return null;

    const errors: PasswordStrengthErrors = {};

    if (value.length < finalConfig.minLength) errors.minLength = true;
    if (finalConfig.requireUppercase && !/[A-Z]/.test(value)) errors.uppercase = true;
    if (finalConfig.requireLowercase && !/[a-z]/.test(value)) errors.lowercase = true;
    if (finalConfig.requireNumber && !/\d/.test(value)) errors.number = true;
    if (finalConfig.requireSpecial && !/[!@#$%^&*(),.?":{}|<>]/.test(value)) errors.special = true;

    return Object.keys(errors).length > 0 ? { passwordStrength: errors } : null;
  };
}
```

**Uso:**

```typescript
// Con configuracion por defecto
password: ['', [Validators.required, passwordStrengthValidator()]]

// Con configuracion personalizada
password: ['', [passwordStrengthValidator({
  minLength: 10,
  requireSpecial: false
})]]
```

**Mostrar errores en template:**

```html
@if (form.controls.password.hasError('passwordStrength')) {
  <ul class="password-requirements">
    @if (form.controls.password.getError('passwordStrength').minLength) {
      <li class="error">Minimo 8 caracteres</li>
    }
    @if (form.controls.password.getError('passwordStrength').uppercase) {
      <li class="error">Al menos una mayuscula</li>
    }
    @if (form.controls.password.getError('passwordStrength').lowercase) {
      <li class="error">Al menos una minuscula</li>
    }
    @if (form.controls.password.getError('passwordStrength').number) {
      <li class="error">Al menos un numero</li>
    }
    @if (form.controls.password.getError('passwordStrength').special) {
      <li class="error">Al menos un caracter especial</li>
    }
  </ul>
}
```

#### 3.2.3 Validador de Confirmacion de Contrasena (Cross-Field)

Este validador se aplica a nivel de FormGroup para comparar dos campos.

**Ubicacion:** `core/validators/sync/password-match.validator.ts`

```typescript
export function passwordMatchValidator(
  passwordField: string = 'password',
  confirmField: string = 'confirmPassword'
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password = control.get(passwordField);
    const confirmPassword = control.get(confirmField);

    if (!password || !confirmPassword) return null;
    if (!password.value || !confirmPassword.value) return null;

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({
        ...confirmPassword.errors,
        passwordMismatch: true
      });
      return { passwordMismatch: true };
    }

    return null;
  };
}
```

**Uso:**

```typescript
this.form = this.fb.group({
  password: ['', [Validators.required, passwordStrengthValidator()]],
  confirmPassword: ['', Validators.required]
}, {
  validators: [passwordMatchValidator('password', 'confirmPassword')]
});
```

```html
@if (form.hasError('passwordMismatch')) {
  <span class="error">Las contrasenas no coinciden</span>
}
```

#### 3.2.4 Validador de NIF/DNI Espanol

Valida el formato y la letra de verificacion del NIF espanol.

**Ubicacion:** `core/validators/sync/nif.validator.ts`

```typescript
const NIF_LETTERS = 'TRWAGMYFPDXBNJZSQVHLCKE';

export function nifValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value?.trim().toUpperCase();
    if (!value) return null;

    // Formato: 8 digitos + 1 letra
    const nifRegex = /^(\d{8})([A-Z])$/;
    const match = value.match(nifRegex);

    if (!match) {
      return { nif: { message: 'Formato invalido (8 digitos + letra)' } };
    }

    // Validar letra de verificacion
    const number = parseInt(match[1], 10);
    const expectedLetter = NIF_LETTERS[number % 23];
    const actualLetter = match[2];

    if (expectedLetter !== actualLetter) {
      return { nif: { message: 'Letra de verificacion incorrecta' } };
    }

    return null;
  };
}
```

**Algoritmo de verificacion:**

```
Letra correcta = TRWAGMYFPDXBNJZSQVHLCKE[numero % 23]

Ejemplo: 12345678Z
- numero = 12345678
- 12345678 % 23 = 14
- TRWAGMYFPDXBNJZSQVHLCKE[14] = 'Z'
- Letra proporcionada = 'Z' ✓ Valido
```

#### 3.2.5 Validador de Telefono

Valida numeros de telefono en formato español e internacional.

**Ubicacion:** `core/validators/sync/custom-pattern.validator.ts`

```typescript
export function phoneValidator(): ValidatorFn {
  return customPatternValidator({
    // Formatos validos:
    // +34612345678, +34 612 345 678, 612345678, 612 345 678
    pattern: /^(\+\d{1,3}\s?)?\d{3}[\s]?\d{3}[\s]?\d{3,4}$/,
    errorKey: 'invalidPhone',
    errorMessage: 'Introduce un numero valido (ej: 612345678 o +34 612 345 678)'
  });
}
```

#### 3.2.6 Validador de Codigo Postal Espanol

```typescript
export function esPostalCodeValidator(): ValidatorFn {
  return customPatternValidator({
    // Codigos postales espanoles: 01000-52999
    pattern: /^(?:0[1-9]|[1-4]\d|5[0-2])\d{3}$/,
    errorKey: 'invalidPostalCode',
    errorMessage: 'Codigo postal invalido'
  });
}
```

#### 3.2.7 Validadores Asincronos

**Validador de Email Unico:**

```typescript
// core/validators/async/email-unique.validator.ts
export function emailUniqueValidator(debounceMs: number = 500): AsyncValidatorFn {
  const takenEmails = ['admin@antipanel.com', 'test@test.com'];

  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const email = control.value;
    if (!email) return of(null);

    return timer(debounceMs).pipe(
      map(() => {
        const isTaken = takenEmails.includes(email.toLowerCase());
        return isTaken ? { emailTaken: true } : null;
      }),
      catchError(() => of(null))
    );
  };
}
```

**Validador de Username Disponible:**

```typescript
// core/validators/async/username-available.validator.ts
export function usernameAvailableValidator(config = {}): AsyncValidatorFn {
  const { debounceMs = 500, minLength = 3 } = config;
  const takenUsernames = ['admin', 'root', 'user', 'test'];

  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const username = control.value?.trim().toLowerCase();
    if (!username || username.length < minLength) return of(null);

    return timer(debounceMs).pipe(
      map(() => takenUsernames.includes(username) ? { usernameTaken: true } : null),
      catchError(() => of(null))
    );
  };
}
```

**Uso con debounce:**

```typescript
this.form = this.fb.group({
  email: this.fb.control('', {
    validators: [Validators.required, Validators.email],
    asyncValidators: [emailUniqueValidator(500)] // 500ms debounce
  }),
  username: this.fb.control('', {
    validators: [Validators.required, Validators.minLength(3)],
    asyncValidators: [usernameAvailableValidator({ debounceMs: 500 })]
  })
});
```

### 3.3 FormArray Dinamico

FormArray permite gestionar colecciones dinamicas de controles, ideal para listas de items, direcciones, telefonos, etc.

#### 3.3.1 Creacion y Estructura

```typescript
protected readonly form = this.fb.group({
  customerName: ['', Validators.required],
  items: this.fb.array([this.createItemGroup()])
});

protected get items(): FormArray {
  return this.form.get('items') as FormArray;
}

private createItemGroup(): FormGroup {
  return this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    quantity: [1, [Validators.required, Validators.min(1)]],
    price: [0, [Validators.required, Validators.min(0)]]
  });
}
```

#### 3.3.2 Agregar y Eliminar Elementos

```typescript
protected addItem(): void {
  this.items.push(this.createItemGroup());
}

protected removeItem(index: number): void {
  if (this.items.length > 1) {
    this.items.removeAt(index);
  }
}
```

#### 3.3.3 Template con Validacion

```html
<form [formGroup]="form">
  <input formControlName="customerName" />

  <div formArrayName="items">
    @for (item of items.controls; track $index; let i = $index) {
      <div [formGroupName]="i" class="item-row">
        <input formControlName="name" placeholder="Nombre" />
        @if (items.at(i).get('name')?.touched && items.at(i).get('name')?.hasError('required')) {
          <span class="error">Nombre requerido</span>
        }

        <input formControlName="quantity" type="number" />
        <input formControlName="price" type="number" />

        <span class="subtotal">
          {{ (item.get('quantity')?.value || 0) * (item.get('price')?.value || 0) | number:'1.2-2' }} €
        </span>

        <button type="button" (click)="removeItem(i)" [disabled]="items.length === 1">
          Eliminar
        </button>
      </div>
    }
  </div>

  <button type="button" (click)="addItem()">Agregar Item</button>

  <div class="total">
    Total: {{ grandTotal() | number:'1.2-2' }} €
  </div>
</form>
```

#### 3.3.4 Calculos con Computed

```typescript
protected readonly grandTotal = computed(() => {
  return this.items.controls.reduce((sum, group) => {
    const qty = group.get('quantity')?.value || 0;
    const price = group.get('price')?.value || 0;
    return sum + (qty * price);
  }, 0);
});
```

#### 3.3.5 Ejemplo: Lista de Telefonos

```typescript
protected readonly phoneForm = this.fb.group({
  phones: this.fb.array([this.createPhoneGroup()])
});

private createPhoneGroup(): FormGroup {
  return this.fb.group({
    type: ['mobile', Validators.required],
    number: ['', [Validators.required, phoneValidator()]]
  });
}
```

```html
<div formArrayName="phones">
  @for (phone of phones.controls; track $index; let i = $index) {
    <div [formGroupName]="i">
      <select formControlName="type">
        <option value="mobile">Movil</option>
        <option value="home">Casa</option>
        <option value="work">Trabajo</option>
      </select>
      <input formControlName="number" />
      <button (click)="removePhone(i)">Eliminar</button>
    </div>
  }
</div>
<button (click)="addPhone()">Agregar Telefono</button>
```

### 3.4 Gestion de Estados del Formulario

#### 3.4.1 Estados touched/dirty

- **pristine/dirty**: Si el valor ha sido modificado
- **touched/untouched**: Si el usuario ha interactuado con el campo
- **valid/invalid**: Si pasa las validaciones
- **pending**: Si hay validaciones async en progreso

**Mostrar errores solo cuando sea apropiado:**

```typescript
protected shouldShowError(controlName: string): boolean {
  const control = this.form.get(controlName);
  return control ? control.invalid && (control.dirty || control.touched) : false;
}
```

```html
<input formControlName="email" />
@if (shouldShowError('email')) {
  @if (form.controls.email.hasError('required')) {
    <span class="error">El email es obligatorio</span>
  } @else if (form.controls.email.hasError('email')) {
    <span class="error">Formato de email invalido</span>
  } @else if (form.controls.email.hasError('emailTaken')) {
    <span class="error">Este email ya esta registrado</span>
  }
}
```

#### 3.4.2 Deshabilitar Submit si Invalido

```typescript
protected readonly submitDisabled = computed(() => {
  return this.form.invalid || this.form.pending || this.isSubmitting();
});
```

```html
<button
  type="submit"
  [disabled]="submitDisabled()"
  [class.loading]="isSubmitting()"
>
  @if (form.pending) {
    Validando...
  } @else if (isSubmitting()) {
    Guardando...
  } @else {
    Guardar
  }
</button>
```

#### 3.4.3 Loading Durante Validacion Async

```html
<div class="input-group">
  <input formControlName="username" />

  @if (form.controls.username.pending) {
    <span class="status checking">
      <app-spinner size="sm" />
      Verificando disponibilidad...
    </span>
  } @else if (form.controls.username.valid && form.controls.username.dirty) {
    <span class="status valid">
      <ng-icon name="matCheck" />
      Disponible
    </span>
  } @else if (form.controls.username.hasError('usernameTaken')) {
    <span class="status error">
      <ng-icon name="matClose" />
      No disponible
    </span>
  }
</div>
```

#### 3.4.4 Feedback Visual Completo

He implementado clases CSS que responden a los estados del formulario:

```scss
.form-input {
  // Estado normal
  border: 1px solid var(--color-secondary);

  // Con focus
  &:focus {
    border-color: var(--color-stats-blue);
    box-shadow: 0 0 0 2px rgba(var(--color-stats-blue-rgb), 0.2);
  }

  // Invalido y tocado
  &.ng-invalid.ng-touched {
    border-color: var(--color-error);
  }

  // Valido y modificado
  &.ng-valid.ng-dirty {
    border-color: var(--color-success);
  }

  // Pendiente (validacion async)
  &.ng-pending {
    border-color: var(--color-warning);
  }
}
```
