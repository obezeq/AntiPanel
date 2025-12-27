import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  ElementRef,
  input,
  output,
  signal,
  viewChild
} from '@angular/core';
import { NgIcon } from '@ng-icons/core';

export interface OrderReadyData {
  matchPercentage: number;
  service: {
    icon: string;
    platform: string;
    type: string;
    quality: string;
    speed: string;
  };
  quantity: number;
  price: string;
  target?: string;
}

@Component({
  selector: 'app-order-ready',
  templateUrl: './order-ready.html',
  styleUrl: './order-ready.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class OrderReady implements AfterViewInit {
  /** Order data to display */
  readonly data = input.required<OrderReadyData>();

  /** Emits when user clicks "EXPLORE MORE" */
  readonly exploreMore = output<void>();

  /** Emits when user clicks "MORE [PLATFORM]" */
  readonly morePlatform = output<string>();

  /** Emits when user clicks "PLACE ORDER" */
  readonly placeOrder = output<OrderReadyData>();

  /** Emits when user changes the target */
  readonly targetChange = output<string>();

  /** Emits when user changes the quantity */
  readonly quantityChange = output<number>();

  /** Whether target is provided (With Target mode) */
  protected readonly hasTarget = computed(() => !!this.data().target);

  /** Platform name for "MORE X" button */
  protected readonly platformName = computed(() => this.data().service.platform);

  /** Whether target is being edited */
  protected readonly isEditingTarget = signal(false);

  /** Whether quantity is being edited */
  protected readonly isEditingQuantity = signal(false);

  /** Reference to target input for auto-focus */
  protected readonly targetInputRef = viewChild<ElementRef<HTMLInputElement>>('targetInput');

  /** Reference to quantity input for auto-focus */
  protected readonly quantityInputRef = viewChild<ElementRef<HTMLInputElement>>('quantityInput');

  constructor() {
    // Auto-focus target input when editing starts
    effect(() => {
      if (this.isEditingTarget()) {
        setTimeout(() => this.targetInputRef()?.nativeElement.focus(), 0);
      }
    });

    // Auto-focus quantity input when editing starts
    effect(() => {
      if (this.isEditingQuantity()) {
        setTimeout(() => this.quantityInputRef()?.nativeElement.focus(), 0);
      }
    });
  }

  ngAfterViewInit(): void {
    // Required for viewChild to work properly
  }

  /** Start editing target */
  protected startTargetEdit(): void {
    this.isEditingTarget.set(true);
  }

  /** Handle target input blur */
  protected onTargetBlur(event: FocusEvent): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.trim();
    if (value && value !== this.data().target) {
      this.targetChange.emit(value);
    }
    this.isEditingTarget.set(false);
  }

  /** Handle target input Enter key */
  protected onTargetSubmit(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value.trim();
    if (value && value !== this.data().target) {
      this.targetChange.emit(value);
    }
    this.isEditingTarget.set(false);
  }

  /** Cancel target edit */
  protected cancelTargetEdit(): void {
    this.isEditingTarget.set(false);
  }

  /** Start editing quantity */
  protected startQuantityEdit(): void {
    this.isEditingQuantity.set(true);
  }

  /** Handle quantity input blur */
  protected onQuantityBlur(event: FocusEvent): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 10);
    if (value > 0 && value !== this.data().quantity) {
      this.quantityChange.emit(value);
    }
    this.isEditingQuantity.set(false);
  }

  /** Handle quantity input Enter key */
  protected onQuantitySubmit(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 10);
    if (value > 0 && value !== this.data().quantity) {
      this.quantityChange.emit(value);
    }
    this.isEditingQuantity.set(false);
  }

  /** Cancel quantity edit */
  protected cancelQuantityEdit(): void {
    this.isEditingQuantity.set(false);
  }
}
