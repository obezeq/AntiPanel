import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
  FormArray,
  FormGroup
} from '@angular/forms';
import { NgIcon } from '@ng-icons/core';

/**
 * Interface for a single item in the FormArray
 */
interface ItemFormGroup {
  name: string;
  quantity: number;
  price: number;
}

/**
 * FormArray Demo Page
 *
 * This page demonstrates the use of FormArray in Angular 21 reactive forms.
 * It shows how to:
 * - Create a FormArray with dynamic items
 * - Add and remove items dynamically
 * - Validate each item individually
 * - Calculate totals in real-time
 * - Handle form submission
 */
@Component({
  selector: 'app-formarray-demo',
  templateUrl: './formarray-demo.html',
  styleUrl: './formarray-demo.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, ReactiveFormsModule, NgIcon]
})
export class FormArrayDemo {
  private readonly fb = inject(NonNullableFormBuilder);

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
   * This is needed to access the array in the template.
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
   * Computed: Calculate subtotal for each item (quantity * price)
   */
  protected readonly itemSubtotals = computed(() => {
    return this.itemControls.map(group => {
      const quantity = group.get('quantity')?.value || 0;
      const price = group.get('price')?.value || 0;
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
   * Computed: Whether we can remove items (minimum 1 required)
   */
  protected readonly canRemoveItem = computed(() => {
    return this.items.length > 1;
  });

  /**
   * Computed: Whether the form is valid
   */
  protected readonly isFormValid = computed(() => {
    return this.form.valid;
  });

  /**
   * Computed: Count of valid items
   */
  protected readonly validItemsCount = computed(() => {
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
  }

  /**
   * Removes an item from the FormArray by index.
   * Minimum 1 item is required.
   */
  protected removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  /**
   * Gets the error message for a specific field in an item.
   */
  protected getItemError(index: number, field: string): string {
    const control = this.itemControls[index]?.get(field);
    if (!control || !control.touched || control.valid) return '';

    if (control.hasError('required')) {
      return `${this.capitalize(field)} is required`;
    }
    if (control.hasError('minlength')) {
      return `${this.capitalize(field)} must be at least 2 characters`;
    }
    if (control.hasError('min')) {
      const min = control.getError('min').min;
      return `${this.capitalize(field)} must be at least ${min}`;
    }
    if (control.hasError('max')) {
      const max = control.getError('max').max;
      return `${this.capitalize(field)} cannot exceed ${max}`;
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
      return 'Customer name is required';
    }
    if (control.hasError('minlength')) {
      return 'Customer name must be at least 2 characters';
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
      this.submissionResult.set('Please fix the errors before submitting.');
      return;
    }

    this.submitted.set(true);

    // Format the submission result
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
    // Clear all items except the first one
    while (this.items.length > 1) {
      this.items.removeAt(1);
    }
    // Reset the first item
    this.items.at(0).reset({
      name: '',
      quantity: 1,
      price: 0
    });
    this.submitted.set(false);
    this.submissionResult.set('');
  }

  /**
   * Helper to capitalize first letter.
   */
  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }
}
