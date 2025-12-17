# Fase 3: Formularios Reactivos Avanzados

## Introduccion

En esta fase he implementado un sistema completo de formularios reactivos utilizando Angular 21. El objetivo principal ha sido crear validadores personalizados reutilizables, implementar validacion asincrona para verificacion de datos en tiempo real, y demostrar el uso de FormArray para contenido dinamico.

He optado por utilizar Reactive Forms en lugar de la nueva API experimental de Signal Forms de Angular 21. Aunque Signal Forms promete simplificar la gestion de formularios, aun se encuentra en fase experimental y su API puede cambiar en futuras versiones. Reactive Forms sigue siendo el estandar estable y ampliamente documentado.

## Arquitectura de la Solucion

### Estructura de Archivos

```
frontend/src/app/
├── core/
│   ├── validators/
│   │   ├── index.ts                    # Barrel export
│   │   ├── sync/
│   │   │   ├── password-strength.validator.ts
│   │   │   ├── password-match.validator.ts
│   │   │   └── custom-pattern.validator.ts
│   │   └── async/
│   │       └── email-unique.validator.ts
│   └── services/
│       └── auth.service.ts
├── components/shared/
│   └── auth-form/
│       ├── auth-form.ts
│       ├── auth-form.html
│       └── auth-form.scss
└── pages/
    └── formarray-demo/
        ├── formarray-demo.ts
        ├── formarray-demo.html
        └── formarray-demo.scss
```

## Catalogo de Validadores

### Validadores Sincronos

#### 1. Password Strength Validator

Este validador verifica que una contrasena cumpla con requisitos minimos de seguridad.

**Ubicacion:** `core/validators/sync/password-strength.validator.ts`

**Requisitos por defecto:**
- Minimo 8 caracteres
- Al menos una letra mayuscula
- Al menos una letra minuscula
- Al menos un numero
- Al menos un caracter especial (!@#$%^&*(),.?":{}|<>)

**Uso basico:**

```typescript
import { passwordStrengthValidator } from '@core/validators';

this.form = this.fb.group({
  password: ['', [Validators.required, passwordStrengthValidator()]]
});
```

**Uso con configuracion personalizada:**

```typescript
this.form = this.fb.group({
  password: ['', [
    passwordStrengthValidator({
      minLength: 10,
      requireUppercase: true,
      requireLowercase: true,
      requireNumber: true,
      requireSpecial: false  // Desactivar requisito de caracter especial
    })
  ]]
});
```

**Obtener mensajes de error legibles:**

```typescript
import { getPasswordStrengthErrors } from '@core/validators';

const errors = control.getError('passwordStrength');
const messages = getPasswordStrengthErrors(errors);
// ['At least 8 characters', 'At least one uppercase letter', ...]
```

#### 2. Password Match Validator

Este validador verifica que dos campos de contrasena coincidan. Es un validador a nivel de grupo (cross-field validation), lo que significa que se aplica al FormGroup en lugar de a controles individuales.

**Ubicacion:** `core/validators/sync/password-match.validator.ts`

**Uso:**

```typescript
import { passwordMatchValidator } from '@core/validators';

this.form = this.fb.group({
  password: ['', Validators.required],
  confirmPassword: ['', Validators.required]
}, {
  validators: [passwordMatchValidator('password', 'confirmPassword')]
});
```

**Verificar error en template:**

```html
@if (form.hasError('passwordMismatch')) {
  <span class="error">Las contrasenas no coinciden</span>
}
```

#### 3. Custom Pattern Validator

Este validador permite crear validaciones basadas en expresiones regulares con mensajes de error personalizados.

**Ubicacion:** `core/validators/sync/custom-pattern.validator.ts`

**Uso basico:**

```typescript
import { customPatternValidator } from '@core/validators';

this.form = this.fb.group({
  phone: ['', [
    customPatternValidator({
      pattern: /^\+?[0-9]{10,15}$/,
      errorKey: 'invalidPhone',
      errorMessage: 'Introduce un numero de telefono valido'
    })
  ]]
});
```

**Validadores preconfigurados:**

El archivo incluye varios validadores listos para usar:

```typescript
import {
  phoneValidator,
  urlValidator,
  slugValidator,
  lettersOnlyValidator,
  creditCardValidator,
  usZipCodeValidator,
  esPostalCodeValidator,
  noWhitespaceValidator
} from '@core/validators';

this.form = this.fb.group({
  phone: ['', phoneValidator()],
  website: ['', urlValidator()],
  username: ['', slugValidator()],
  name: ['', lettersOnlyValidator()],
  postalCode: ['', esPostalCodeValidator()]
});
```

### Validadores Asincronos

#### Email Unique Validator

Este validador verifica de forma asincrona si un email ya esta registrado en el sistema.

**Ubicacion:** `core/validators/async/email-unique.validator.ts`

**Importante:** En la implementacion actual, el validador utiliza una lista simulada de emails para demostracion. En un entorno de produccion, deberia conectarse a un endpoint real del backend.

**Uso:**

```typescript
import { emailUniqueValidator } from '@core/validators';

this.form = this.fb.group({
  email: ['', {
    validators: [Validators.required, Validators.email],
    asyncValidators: [emailUniqueValidator(500)], // 500ms debounce
    updateOn: 'blur'  // Validar al salir del campo
  }]
});
```

**Emails de prueba que generan error:**
- admin@antipanel.com
- test@test.com
- user@example.com
- demo@demo.com

**Mostrar estado de validacion:**

```html
@if (form.controls.email.pending) {
  <span>Verificando email...</span>
}
@if (form.controls.email.hasError('emailTaken')) {
  <span class="error">Este email ya esta registrado</span>
}
```

## Guia de Uso de FormArray

FormArray es una estructura de Angular que permite manejar colecciones dinamicas de controles de formulario. A diferencia de FormGroup, que tiene un conjunto fijo de claves, FormArray es un array indexado que puede crecer o reducirse en tiempo de ejecucion.

### Creacion de FormArray

**Archivo:** `pages/formarray-demo/formarray-demo.ts`

```typescript
import { FormArray, FormGroup, NonNullableFormBuilder } from '@angular/forms';

@Component({...})
export class FormArrayDemo {
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly form = this.fb.group({
    customerName: ['', [Validators.required]],
    items: this.fb.array([
      this.createItemGroup()  // Item inicial
    ])
  });

  // Getter para acceder al FormArray
  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  // Crear un grupo de item
  private createItemGroup(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      quantity: [1, [Validators.required, Validators.min(1)]],
      price: [0, [Validators.required, Validators.min(0)]]
    });
  }
}
```

### Operaciones Dinamicas

**Agregar item:**

```typescript
addItem(): void {
  this.items.push(this.createItemGroup());
}
```

**Eliminar item:**

```typescript
removeItem(index: number): void {
  if (this.items.length > 1) {
    this.items.removeAt(index);
  }
}
```

### Iteracion en Template

```html
<div formArrayName="items">
  @for (item of items.controls; track $index; let i = $index) {
    <div [formGroupName]="i" class="item">
      <input formControlName="name" placeholder="Nombre" />
      <input formControlName="quantity" type="number" />
      <input formControlName="price" type="number" />
      <button type="button" (click)="removeItem(i)">Eliminar</button>
    </div>
  }
</div>
<button type="button" (click)="addItem()">Agregar Item</button>
```

### Validacion Individual

Cada item del FormArray puede tener su propia validacion:

```typescript
getItemError(index: number, field: string): string {
  const control = this.items.at(index).get(field);
  if (!control || !control.touched || control.valid) return '';

  if (control.hasError('required')) {
    return 'Este campo es obligatorio';
  }
  if (control.hasError('min')) {
    return `El valor minimo es ${control.getError('min').min}`;
  }
  return '';
}
```

### Calculos en Tiempo Real

```typescript
// Subtotal de cada item
protected readonly itemSubtotals = computed(() => {
  return this.items.controls.map(group => {
    const quantity = group.get('quantity')?.value || 0;
    const price = group.get('price')?.value || 0;
    return quantity * price;
  });
});

// Total general
protected readonly grandTotal = computed(() => {
  return this.itemSubtotals().reduce((sum, subtotal) => sum + subtotal, 0);
});
```

## Validacion Asincrona

### Patron de Implementacion

La validacion asincrona en Angular sigue este patron:

1. El validador devuelve un Observable que emite `ValidationErrors | null`
2. Incluir debounce para evitar multiples llamadas
3. Cancelar peticiones anteriores con `switchMap`
4. Manejar errores de red graciosamente

**Ejemplo completo:**

```typescript
export function emailUniqueValidator(debounceMs: number = 500): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const email = control.value as string;

    // No validar valores vacios
    if (!email) {
      return of(null);
    }

    // Debounce y peticion
    return timer(debounceMs).pipe(
      switchMap(() => checkEmailExists(email)),
      map(exists => exists ? { emailTaken: true } : null),
      catchError(() => of(null))  // En caso de error, no bloquear
    );
  };
}
```

### Estados de Validacion

El control de formulario expone tres estados importantes:

- `control.pending`: La validacion asincrona esta en progreso
- `control.valid`: El control es valido
- `control.invalid`: El control tiene errores

```html
<div class="input-wrapper">
  <input formControlName="email" />

  @if (form.controls.email.pending) {
    <span class="status pending">Verificando...</span>
  } @else if (form.controls.email.valid) {
    <span class="status valid">Disponible</span>
  } @else if (form.controls.email.hasError('emailTaken')) {
    <span class="status error">Email ya registrado</span>
  }
</div>
```

## Acceso a Elementos del DOM

### ViewChild y ElementRef

Angular proporciona `ViewChild` y `ElementRef` para acceder directamente a elementos del DOM cuando es necesario.

**Uso basico:**

```typescript
import { Component, ViewChild, ElementRef, AfterViewInit } from '@angular/core';

@Component({...})
export class MyComponent implements AfterViewInit {
  @ViewChild('emailInput') emailInput!: ElementRef<HTMLInputElement>;

  ngAfterViewInit(): void {
    // El elemento esta disponible aqui
    this.emailInput.nativeElement.focus();
  }

  focusEmailInput(): void {
    this.emailInput.nativeElement.focus();
  }
}
```

```html
<input #emailInput type="email" formControlName="email" />
```

**Consideraciones:**
- `ViewChild` no esta disponible hasta `ngAfterViewInit`
- Evitar manipular el DOM directamente cuando sea posible
- Preferir bindings y directivas sobre manipulacion directa

## Gestion de Estados de Formulario

### Estados de Control

Cada control de formulario tiene estados que pueden utilizarse para mostrar feedback:

- **touched/untouched**: Si el usuario ha interactuado con el campo
- **dirty/pristine**: Si el valor ha sido modificado
- **valid/invalid**: Si pasa las validaciones
- **pending**: Si hay validaciones asincronas en progreso

### Practica Recomendada

Solo mostrar errores despues de que el usuario haya interactuado:

```typescript
// En el componente
protected readonly emailTouched = signal(false);

protected onEmailBlur(): void {
  this.emailTouched.set(true);
}

protected readonly emailError = computed(() => {
  if (!this.emailTouched()) return '';

  const control = this.form.controls.email;
  if (control.hasError('required')) return 'El email es obligatorio';
  if (control.hasError('email')) return 'Formato de email invalido';
  return '';
});
```

```html
<input
  formControlName="email"
  (blur)="onEmailBlur()"
  [class.error]="emailError()"
/>
@if (emailError()) {
  <span class="error-message">{{ emailError() }}</span>
}
```

### Deshabilitar Submit

```typescript
protected readonly submitDisabled = computed(() => {
  return this.loading() || this.form.invalid || this.form.pending;
});
```

```html
<button type="submit" [disabled]="submitDisabled()">
  @if (form.pending) {
    Validando...
  } @else {
    Enviar
  }
</button>
```

## Mejores Practicas Aplicadas

### 1. NonNullableFormBuilder

He utilizado `NonNullableFormBuilder` en lugar de `FormBuilder` para obtener mejor inferencia de tipos:

```typescript
private readonly fb = inject(NonNullableFormBuilder);
```

### 2. Signals para Estado Local

Los signals de Angular permiten gestionar el estado local de forma reactiva:

```typescript
protected readonly emailTouched = signal(false);
protected readonly loading = signal(false);
```

### 3. Computed para Valores Derivados

Los valores calculados se actualizan automaticamente cuando cambian sus dependencias:

```typescript
protected readonly submitDisabled = computed(() => {
  return this.loading() || this.form.invalid || this.form.pending;
});
```

### 4. Effects para Logica Reactiva

Los effects permiten ejecutar codigo cuando cambian los signals:

```typescript
effect(() => {
  const isRegister = this.isRegisterMode();
  if (isRegister) {
    this.form.controls.email.setAsyncValidators([emailUniqueValidator()]);
  } else {
    this.form.controls.email.clearAsyncValidators();
  }
});
```

### 5. Validadores Reutilizables

Todos los validadores estan en una carpeta centralizada con barrel exports:

```typescript
// Importar lo que necesites
import {
  passwordStrengthValidator,
  passwordMatchValidator,
  emailUniqueValidator,
  phoneValidator
} from '@core/validators';
```

## Formularios Implementados

### 1. AuthForm (Login/Register)

**Ubicacion:** `components/shared/auth-form/`

**Caracteristicas:**
- Modo login con email y password
- Modo registro con validacion de fuerza de contrasena
- Confirmacion de contrasena con validacion cruzada
- Validacion asincrona de email unico (en modo registro)
- Feedback visual de estados de validacion
- Indicadores de fuerza de contrasena
- Estados de carga durante validacion

### 2. FormArray Demo

**Ubicacion:** `pages/formarray-demo/`
**Ruta:** `/formarray`

**Caracteristicas:**
- FormArray con items dinamicos
- Agregar y eliminar items
- Validacion individual por item
- Calculo de subtotales en tiempo real
- Total general calculado
- Documentacion integrada sobre como funciona FormArray

## Conclusiones

Esta implementacion proporciona una base solida para la gestion de formularios en la aplicacion AntiPanel. Los validadores son reutilizables y configurables, el sistema de estados permite feedback visual claro, y la arquitectura sigue las mejores practicas de Angular 21.

El uso de Reactive Forms garantiza compatibilidad y estabilidad, mientras que la integracion con Signals permite una gestion de estado moderna y eficiente. La documentacion en el Style Guide y la pagina de demostracion de FormArray sirven como referencia practica para futuros desarrollos.
