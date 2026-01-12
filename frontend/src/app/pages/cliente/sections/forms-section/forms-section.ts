import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
  FormArray,
  FormGroup
} from '@angular/forms';
import { NgIcon } from '@ng-icons/core';
import { startWith } from 'rxjs';

// Importar validadores personalizados
import {
  nifValidator,
  getNifError,
  usernameAvailableValidator,
  usernameFormatValidator,
  getUsernameTakenError,
  phoneValidator,
  esPostalCodeValidator,
  urlValidator,
  slugValidator,
  lettersOnlyValidator,
  creditCardValidator,
  noWhitespaceValidator
} from '../../../../core/validators';

/**
 * Interface for a single item in the FormArray
 */
interface ItemFormGroup {
  name: string;
  quantity: number;
  price: number;
}

/**
 * Forms Section - Fase 3
 *
 * Demuestra:
 * - FormArray con items dinamicos
 * - Validadores sincronos personalizados (NIF, telefono, codigo postal)
 * - Validadores asincronos (username disponible)
 * - Estados de formulario (touched, dirty, pending)
 * - Feedback visual de validacion
 */
@Component({
  selector: 'app-forms-section',
  templateUrl: './forms-section.html',
  styleUrl: './forms-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, NgIcon]
})
export class FormsSection {
  private readonly fb = inject(NonNullableFormBuilder);

  // =========================================================================
  // FORMULARIO DE REGISTRO CON VALIDADORES PERSONALIZADOS
  // =========================================================================

  /** Formulario de registro con validadores personalizados */
  protected readonly registrationForm = this.fb.group({
    username: ['', {
      validators: [Validators.required, Validators.minLength(3), usernameFormatValidator()],
      asyncValidators: [usernameAvailableValidator({ debounceMs: 500 })],
      updateOn: 'blur' as const
    }],
    nif: ['', [Validators.required, nifValidator()]],
    phone: ['', [Validators.required, phoneValidator()]],
    postalCode: ['', [Validators.required, esPostalCodeValidator()]]
  });

  /** Signal para estado touched de username */
  protected readonly usernameTouched = signal(false);

  /** Computed: si el username esta siendo validado */
  protected readonly isValidatingUsername = computed(() => {
    return this.registrationForm.controls.username.pending;
  });

  /** Obtener errores de username formateados */
  protected getUsernameError(): string | null {
    const control = this.registrationForm.controls.username;
    if (!this.usernameTouched() || control.valid || control.pending) return null;

    if (control.hasError('required')) return 'El nombre de usuario es obligatorio';
    if (control.hasError('minlength')) return 'Minimo 3 caracteres';
    if (control.hasError('usernameFormat')) return control.getError('usernameFormat').message;
    if (control.hasError('usernameTaken')) return getUsernameTakenError();
    return null;
  }

  /** Obtener errores de NIF formateados */
  protected getNifError(): string | null {
    const control = this.registrationForm.controls.nif;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'El NIF/NIE es obligatorio';
    if (control.hasError('nif')) return getNifError(control.errors);
    return null;
  }

  /** Obtener errores de telefono */
  protected getPhoneError(): string | null {
    const control = this.registrationForm.controls.phone;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'El telefono es obligatorio';
    if (control.hasError('invalidPhone')) return control.getError('invalidPhone').message;
    return null;
  }

  /** Obtener errores de codigo postal */
  protected getPostalCodeError(): string | null {
    const control = this.registrationForm.controls.postalCode;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'El codigo postal es obligatorio';
    if (control.hasError('invalidPostalCode')) return control.getError('invalidPostalCode').message;
    return null;
  }

  /** Marcar username como touched */
  protected onUsernameBlur(): void {
    this.usernameTouched.set(true);
  }

  /** Resultado de envio del formulario de registro */
  protected readonly registrationResult = signal<string>('');

  /** Enviar formulario de registro */
  protected onRegistrationSubmit(): void {
    this.registrationForm.markAllAsTouched();
    this.usernameTouched.set(true);

    if (this.registrationForm.invalid || this.registrationForm.pending) {
      this.registrationResult.set('Por favor corrige los errores antes de enviar.');
      return;
    }

    const data = this.registrationForm.getRawValue();
    this.registrationResult.set(JSON.stringify(data, null, 2));
  }

  /** Resetear formulario de registro */
  protected resetRegistrationForm(): void {
    this.registrationForm.reset();
    this.usernameTouched.set(false);
    this.registrationResult.set('');
  }

  // =========================================================================
  // FORMARRAY - PEDIDO DINAMICO
  // =========================================================================

  /** Whether form has been submitted */
  protected readonly submitted = signal(false);

  /** Submission result for display */
  protected readonly submissionResult = signal<string>('');

  /**
   * Main form with a FormArray of items.
   * Each item has: name, quantity, price
   */
  protected readonly form = this.fb.group({
    customerName: ['', [Validators.required, Validators.minLength(2)]],
    items: this.fb.array([
      this.createItemGroup()
    ])
  });

  /**
   * Getter for the items FormArray.
   */
  get items(): FormArray {
    return this.form.get('items') as FormArray;
  }

  /**
   * Get items as array of FormGroups for iteration.
   */
  get itemControls(): FormGroup[] {
    return this.items.controls as FormGroup[];
  }

  /**
   * Signal from form valueChanges to enable reactive computations.
   */
  private readonly formValues = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.getRawValue())),
    { initialValue: this.form.getRawValue() }
  );

  /**
   * Computed: Calculate subtotal for each item (quantity * price)
   */
  protected readonly itemSubtotals = computed(() => {
    const values = this.formValues();
    return (values.items || []).map((item: ItemFormGroup) => {
      const quantity = item.quantity || 0;
      const price = item.price || 0;
      return quantity * price;
    });
  });

  /**
   * Computed: Calculate grand total of all items
   */
  protected readonly grandTotal = computed(() => {
    return this.itemSubtotals().reduce((sum, subtotal) => sum + subtotal, 0);
  });

  /**
   * Signal to track items count for reactivity
   */
  protected readonly itemsCount = signal(1);

  /**
   * Computed: Whether we can remove items (minimum 1 required)
   */
  protected readonly canRemoveItem = computed(() => {
    return this.itemsCount() > 1;
  });

  /**
   * Computed: Whether the form is valid
   */
  protected readonly isFormValid = computed(() => {
    this.formValues();
    return this.form.valid;
  });

  /**
   * Computed: Count of valid items
   */
  protected readonly validItemsCount = computed(() => {
    this.formValues();
    return this.itemControls.filter(group => group.valid).length;
  });

  /**
   * Creates a new item FormGroup with validators.
   */
  private createItemGroup(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      quantity: [1, [Validators.required, Validators.min(1), Validators.max(100)]],
      price: [0, [Validators.required, Validators.min(0)]]
    });
  }

  /**
   * Adds a new item to the FormArray.
   */
  protected addItem(): void {
    this.items.push(this.createItemGroup());
    this.itemsCount.set(this.items.length);
  }

  /**
   * Removes an item from the FormArray by index.
   */
  protected removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
      this.itemsCount.set(this.items.length);
    }
  }

  /**
   * Gets the error message for a specific field in an item.
   */
  protected getItemError(index: number, field: string): string {
    const control = this.itemControls[index]?.get(field);
    if (!control || !control.touched || control.valid) return '';

    if (control.hasError('required')) {
      return `${this.capitalize(field)} es obligatorio`;
    }
    if (control.hasError('minlength')) {
      return `${this.capitalize(field)} debe tener al menos 2 caracteres`;
    }
    if (control.hasError('min')) {
      const min = control.getError('min').min;
      return `${this.capitalize(field)} debe ser al menos ${min}`;
    }
    if (control.hasError('max')) {
      const max = control.getError('max').max;
      return `${this.capitalize(field)} no puede superar ${max}`;
    }
    return '';
  }

  /**
   * Gets error for customer name field.
   */
  protected getCustomerNameError(): string {
    const control = this.form.get('customerName');
    if (!control || !control.touched || control.valid) return '';

    if (control.hasError('required')) {
      return 'El nombre del cliente es obligatorio';
    }
    if (control.hasError('minlength')) {
      return 'El nombre debe tener al menos 2 caracteres';
    }
    return '';
  }

  /**
   * Checks if a specific item field is invalid and touched.
   */
  protected isItemFieldInvalid(index: number, field: string): boolean {
    const control = this.itemControls[index]?.get(field);
    return control ? control.invalid && control.touched : false;
  }

  /**
   * Handles form submission.
   */
  protected onSubmit(): void {
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      this.submissionResult.set('Por favor corrige los errores antes de enviar.');
      return;
    }

    this.submitted.set(true);

    const data = this.form.getRawValue();
    const result = {
      customerName: data.customerName,
      items: data.items,
      itemCount: data.items.length,
      grandTotal: this.grandTotal()
    };

    this.submissionResult.set(JSON.stringify(result, null, 2));
  }

  /**
   * Resets the form to initial state.
   */
  protected resetForm(): void {
    this.form.reset();
    while (this.items.length > 1) {
      this.items.removeAt(1);
    }
    this.items.at(0).reset({
      name: '',
      quantity: 1,
      price: 0
    });
    this.itemsCount.set(1);
    this.submitted.set(false);
    this.submissionResult.set('');
  }

  /**
   * Helper to capitalize first letter.
   */
  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  // =========================================================================
  // FORMULARIO EXTENDIDO CON VALIDADORES ADICIONALES
  // =========================================================================

  /** Formulario con validadores adicionales no usados en otras partes */
  protected readonly extendedValidatorsForm = this.fb.group({
    websiteUrl: ['', [Validators.required, urlValidator()]],
    slug: ['', [Validators.required, slugValidator()]],
    fullName: ['', [Validators.required, lettersOnlyValidator()]],
    creditCard: ['', [Validators.required, creditCardValidator()]],
    password: ['', [Validators.required, Validators.minLength(8), noWhitespaceValidator()]]
  });

  /** Resultado del formulario extendido */
  protected readonly extendedResult = signal<string>('');

  /** Obtener error de URL */
  protected getUrlError(): string | null {
    const control = this.extendedValidatorsForm.controls.websiteUrl;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'La URL es obligatoria';
    if (control.hasError('invalidUrl')) return control.getError('invalidUrl').message;
    return null;
  }

  /** Obtener error de slug */
  protected getSlugError(): string | null {
    const control = this.extendedValidatorsForm.controls.slug;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'El slug es obligatorio';
    if (control.hasError('invalidSlug')) return control.getError('invalidSlug').message;
    return null;
  }

  /** Obtener error de nombre completo */
  protected getFullNameError(): string | null {
    const control = this.extendedValidatorsForm.controls.fullName;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'El nombre es obligatorio';
    if (control.hasError('lettersOnly')) return control.getError('lettersOnly').message;
    return null;
  }

  /** Obtener error de tarjeta de credito */
  protected getCreditCardError(): string | null {
    const control = this.extendedValidatorsForm.controls.creditCard;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'La tarjeta es obligatoria';
    if (control.hasError('invalidCreditCard')) return control.getError('invalidCreditCard').message;
    return null;
  }

  /** Obtener error de contrasena */
  protected getPasswordError(): string | null {
    const control = this.extendedValidatorsForm.controls.password;
    if (!control.touched || control.valid) return null;

    if (control.hasError('required')) return 'La contrasena es obligatoria';
    if (control.hasError('minlength')) return 'Minimo 8 caracteres';
    if (control.hasError('hasWhitespace')) return control.getError('hasWhitespace').message;
    return null;
  }

  /** Enviar formulario extendido */
  protected onExtendedSubmit(): void {
    this.extendedValidatorsForm.markAllAsTouched();

    if (this.extendedValidatorsForm.invalid) {
      this.extendedResult.set('Por favor corrige los errores antes de enviar.');
      return;
    }

    const data = this.extendedValidatorsForm.getRawValue();
    // Ocultar la tarjeta de credito por seguridad
    const safeData = {
      ...data,
      creditCard: data.creditCard.replace(/\d(?=\d{4})/g, '*'),
      password: '********'
    };
    this.extendedResult.set(JSON.stringify(safeData, null, 2));
  }

  /** Resetear formulario extendido */
  protected resetExtendedForm(): void {
    this.extendedValidatorsForm.reset();
    this.extendedResult.set('');
  }
}
