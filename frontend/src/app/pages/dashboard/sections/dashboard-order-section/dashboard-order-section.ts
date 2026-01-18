import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { of } from 'rxjs';
import { DashboardSectionHeader } from '../../../../components/shared/dashboard-section-header/dashboard-section-header';
import {
  OrderReady,
  type OrderReadyData,
  type QuantityValidationError
} from '../../../../components/shared/order-ready/order-ready';
import { OrderPlaced } from '../../../../components/shared/order-placed/order-placed';
import { CatalogService } from '../../../../services/catalog.service';
import { OrderService, type OrderCreateRequest, type OrderResponse } from '../../../../core/services/order.service';
import { OrderParserService } from '../../../../core/services/order-parser.service';
import type { ParsedOrder } from '../../../../core/models/order-parser.models';
import type { Service } from '../../../../models';
import type { ServiceItemData } from '../../../../components/shared/service-item-card/service-item-card';

/**
 * Order creation state
 */
type OrderState = 'idle' | 'loading' | 'success' | 'error';

/**
 * Dashboard order section component.
 * Similar to home order section but with balance checking and API integration.
 *
 * @example
 * <app-dashboard-order-section
 *   [userBalance]="balance()"
 *   [quickOrderService]="quickOrderData()"
 *   (orderCreated)="onOrderCreated($event)"
 *   (navigateToWallet)="goToWallet()"
 * />
 */
@Component({
  selector: 'app-dashboard-order-section',
  templateUrl: './dashboard-order-section.html',
  styleUrl: './dashboard-order-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DashboardSectionHeader, OrderReady, OrderPlaced]
})
export class DashboardOrderSection {
  private readonly catalogService = inject(CatalogService);
  private readonly orderService = inject(OrderService);
  private readonly orderParser = inject(OrderParserService);
  private readonly destroyRef = inject(DestroyRef);

  /** Track last processed quick order to avoid re-processing */
  private lastProcessedQuickOrder: ServiceItemData | null = null;

  // -------------------------------------------------------------------------
  // Inputs
  // -------------------------------------------------------------------------

  /** User's current balance for frontend validation */
  readonly userBalance = input.required<number>();

  /** Quick order service passed from ServicesSection via Dashboard */
  readonly quickOrderService = input<ServiceItemData | null>(null);

  /** Initial input text for pre-filling (from pending order flow) */
  readonly initialInputText = input<string | null>(null);

  // -------------------------------------------------------------------------
  // Outputs
  // -------------------------------------------------------------------------

  /** Emits when order is successfully created */
  readonly orderCreated = output<OrderResponse>();

  /** Emits required amount when user needs to navigate to wallet (insufficient balance) */
  readonly navigateToWallet = output<number>();

  /** Emits platform selection for services section */
  readonly selectPlatform = output<string>();

  /** Emits to reset platform selection in services section */
  readonly resetPlatform = output<void>();

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  /** Current user input text */
  protected readonly inputText = signal<string>('');

  /** Whether the input field is focused */
  protected readonly isInputFocused = signal<boolean>(false);

  /** Current order creation state */
  protected readonly orderState = signal<OrderState>('idle');

  /** Error message for failed orders */
  protected readonly errorMessage = signal<string | null>(null);

  /** Success message after order creation */
  protected readonly successMessage = signal<string | null>(null);

  /** Service matching the parsed order */
  protected readonly matchedService = signal<Service | null>(null);

  /** Whether to show the order placed success modal */
  protected readonly showOrderPlacedModal = signal<boolean>(false);

  // -------------------------------------------------------------------------
  // Computed
  // -------------------------------------------------------------------------

  /** Parsed order from input text */
  protected readonly parsedOrder = computed<ParsedOrder>(() => {
    return this.orderParser.parse(this.inputText());
  });

  /** Whether we have enough data to show the order preview */
  protected readonly showOrderReady = computed<boolean>(() => {
    const parsed = this.parsedOrder();
    return parsed.matchPercentage >= 50;
  });

  /** Order ready data for the OrderReady component */
  protected readonly orderReadyData = computed<OrderReadyData | null>(() => {
    const parsed = this.parsedOrder();
    const service = this.matchedService();

    if (!service || !parsed.quantity) {
      return null;
    }

    const price = (service.pricePerK * parsed.quantity) / 1000;

    // Validate quantity against service limits
    let validationError: QuantityValidationError | undefined;

    if (parsed.quantity < service.minQuantity) {
      validationError = {
        type: 'min',
        message: `Minimum quantity is ${service.minQuantity.toLocaleString()}`,
        limit: service.minQuantity
      };
    } else if (parsed.quantity > service.maxQuantity) {
      validationError = {
        type: 'max',
        message: `Maximum quantity is ${service.maxQuantity.toLocaleString()}`,
        limit: service.maxQuantity
      };
    }

    return {
      matchPercentage: parsed.matchPercentage,
      service: {
        icon: this.catalogService.getPlatformIcon(parsed.platform ?? ''),
        platform: this.orderParser.getPlatformDisplayName(parsed.platform ?? ''),
        type: this.orderParser.getServiceTypeDisplayName(parsed.serviceType ?? ''),
        quality: `${service.quality} Quality`,
        speed: `${service.speed} Speed`
      },
      quantity: parsed.quantity,
      price: this.formatPrice(price),
      target: parsed.target ?? undefined,
      validationError
    };
  });

  /** Whether user has sufficient balance for the current order */
  protected readonly hasSufficientBalance = computed<boolean>(() => {
    const data = this.orderReadyData();
    if (!data) return true;

    const price = parseFloat(data.price.replace('$', ''));
    return this.userBalance() >= price;
  });

  /** Whether the place order button should be disabled */
  protected readonly isPlaceOrderDisabled = computed<boolean>(() => {
    const data = this.orderReadyData();
    return this.orderState() === 'loading' ||
           !this.hasSufficientBalance() ||
           !!data?.validationError;
  });

  constructor() {
    // Debounced service lookup - prevents rapid API calls during typing
    // Best practice: Use toObservable + debounceTime for signal-to-HTTP patterns
    // Reference: https://dev.to/agroupp/effective-debouncing-in-angular-keep-signals-pure-56mo
    toObservable(this.parsedOrder)
      .pipe(
        debounceTime(300), // Wait 300ms after last keystroke
        distinctUntilChanged((prev, curr) =>
          prev.platform === curr.platform &&
          prev.serviceType === curr.serviceType
        ),
        switchMap(parsed => {
          if (parsed.platform && parsed.serviceType) {
            return this.catalogService.findService(parsed.platform, parsed.serviceType);
          }
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(service => {
        this.matchedService.set(service ?? null);
      });

    // Watch quickOrderService and populate input text
    effect(() => {
      const quickOrder = this.quickOrderService();
      if (quickOrder && quickOrder !== this.lastProcessedQuickOrder) {
        this.lastProcessedQuickOrder = quickOrder;
        // Format: "1k Instagram Followers"
        const text = `1k ${quickOrder.name}`;
        this.inputText.set(text);
        // Clear any previous messages
        this.errorMessage.set(null);
        this.successMessage.set(null);
      }
    });

    // Watch initialInputText for pending order flow
    effect(() => {
      const text = this.initialInputText();
      if (text) {
        this.inputText.set(text);
        this.errorMessage.set(null);
        this.successMessage.set(null);
      }
    });
  }

  // -------------------------------------------------------------------------
  // Event Handlers
  // -------------------------------------------------------------------------

  protected onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.inputText.set(target.value);
    // Clear messages when user modifies input
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.orderState.set('idle');
  }

  protected onInputFocus(): void {
    this.isInputFocused.set(true);
  }

  protected onInputBlur(): void {
    this.isInputFocused.set(false);
  }

  protected onInputSubmit(): void {
    // Submit via OrderReady component
  }

  protected onExploreMore(): void {
    this.resetPlatform.emit();
  }

  protected onMorePlatform(platform: string): void {
    this.selectPlatform.emit(platform.toLowerCase());
  }

  protected onTargetChange(newTarget: string): void {
    const currentText = this.inputText();
    const parsed = this.parsedOrder();

    let newText: string;
    if (parsed.target) {
      newText = currentText.replace(parsed.target, newTarget);
    } else {
      newText = `${currentText} ${newTarget}`;
    }
    this.inputText.set(newText);
  }

  protected onQuantityChange(newQuantity: number): void {
    const currentText = this.inputText();
    const formatted = this.formatQuantity(newQuantity);
    const quantityPattern = /\d+(?:\.\d+)?\s*(k|m)?/i;
    const newText = currentText.replace(quantityPattern, formatted);
    this.inputText.set(newText);
  }

  private formatQuantity(qty: number): string {
    if (qty >= 1000000) {
      const value = qty / 1000000;
      return Number.isInteger(value) ? `${value}m` : `${value.toFixed(1)}m`;
    }
    if (qty >= 1000) {
      const value = qty / 1000;
      return Number.isInteger(value) ? `${value}k` : `${value.toFixed(1)}k`;
    }
    return qty.toString();
  }

  /**
   * Format price with appropriate precision
   * Shows 2 decimals for prices >= $0.01, up to 6 decimals for smaller amounts
   */
  private formatPrice(price: number): string {
    if (price >= 0.01) {
      return `$${price.toFixed(2)}`;
    }
    // For very small amounts, show up to 6 decimals but trim trailing zeros
    const formatted = price.toFixed(6).replace(/0+$/, '').replace(/\.$/, '');
    return `$${formatted}`;
  }

  /**
   * Handle place order request from OrderReady component.
   * Validates balance and creates order via API.
   * Includes debounce guard to prevent double-click 409 errors.
   */
  protected onPlaceOrder(data: OrderReadyData): void {
    // Prevent double-click submissions (fixes 409 Conflict errors)
    if (this.orderState() === 'loading') {
      return;
    }

    const service = this.matchedService();
    const parsed = this.parsedOrder();

    if (!service || !parsed.quantity || !parsed.target) {
      this.errorMessage.set('Please fill in all order details including target URL or username.');
      return;
    }

    // Frontend balance check
    if (!this.hasSufficientBalance()) {
      this.errorMessage.set('Insufficient balance. Please add funds to continue.');
      return;
    }

    // Set loading state BEFORE creating request to prevent race conditions
    this.orderState.set('loading');
    this.errorMessage.set(null);

    // Create order request
    const request: OrderCreateRequest = {
      serviceId: service.id,
      target: parsed.target,
      quantity: parsed.quantity
    };

    this.orderService.createOrder(request)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.orderState.set('success');
          this.showOrderPlacedModal.set(true);
          this.orderCreated.emit(response);
        },
        error: (error) => {
          this.orderState.set('error');
          if (this.orderService.isInsufficientBalanceError(error)) {
            this.errorMessage.set('Insufficient balance. Please add funds to continue.');
          } else if (this.orderService.isDuplicateOrderError(error)) {
            this.errorMessage.set('Duplicate order detected. Please wait before trying again.');
          } else if (this.orderService.isServiceUnavailableError(error)) {
            this.errorMessage.set('Service is temporarily unavailable. Please try again later or contact support.');
          } else {
            this.errorMessage.set('Failed to create order. Please try again.');
          }
        }
      });
  }

  /**
   * Navigate to wallet to add funds.
   * Emits the required amount (order price - current balance, minimum $1).
   */
  protected onAddFundsClick(): void {
    const data = this.orderReadyData();
    const price = data ? parseFloat(data.price.replace('$', '')) : 0;
    const needed = Math.max(1, price - this.userBalance());
    this.navigateToWallet.emit(needed);
  }

  /**
   * Handle order placed modal continue button.
   * Resets the form state for a new order.
   */
  protected onOrderPlacedContinue(): void {
    this.showOrderPlacedModal.set(false);
    this.orderState.set('idle');
    this.inputText.set('');
    this.successMessage.set(null);
    this.matchedService.set(null);
    this.lastProcessedQuickOrder = null;
  }
}
