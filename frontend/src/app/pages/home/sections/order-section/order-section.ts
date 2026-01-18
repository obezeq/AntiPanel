import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { Router } from '@angular/router';
import { DashboardSectionHeader } from '../../../../components/shared/dashboard-section-header/dashboard-section-header';
import {
  OrderReady,
  type OrderReadyData,
  type QuantityValidationError
} from '../../../../components/shared/order-ready/order-ready';
import { CatalogService } from '../../../../services/catalog.service';
import { OrderParserService } from '../../../../core/services/order-parser.service';
import type { ParsedOrder } from '../../../../core/models/order-parser.models';
import type { Service } from '../../../../models';
import type { ServiceItemData } from '../../../../components/shared/service-item-card/service-item-card';

/**
 * OrderSection component for the home page.
 * Contains the order input and displays the parsed order preview.
 *
 * @example
 * <app-order-section
 *   (placeOrder)="handlePlaceOrder($event)"
 * />
 */
@Component({
  selector: 'app-order-section',
  templateUrl: './order-section.html',
  styleUrl: './order-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DashboardSectionHeader, OrderReady]
})
export class OrderSection {
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);
  private readonly orderParser = inject(OrderParserService);

  /** Track last processed quick order to avoid re-processing */
  private lastProcessedQuickOrder: ServiceItemData | null = null;

  /** Quick order service passed from ServicesSection via Home */
  readonly quickOrderService = input<ServiceItemData | null>(null);

  /** Emits when user wants to place an order (redirects to signup on home) */
  readonly placeOrder = output<OrderReadyData>();

  /** Emits when user clicks "More [Platform]" to select that platform */
  readonly selectPlatform = output<string>();

  /** Emits when user clicks "Explore More" to reset platform selection */
  readonly resetPlatform = output<void>();

  /** Current user input text */
  protected readonly inputText = signal<string>('');

  /** Whether the input field is focused */
  protected readonly isInputFocused = signal<boolean>(false);

  /** Parsed order from input text */
  protected readonly parsedOrder = computed<ParsedOrder>(() => {
    return this.orderParser.parse(this.inputText());
  });

  /** Whether we have enough data to show the order preview */
  protected readonly showOrderReady = computed<boolean>(() => {
    const parsed = this.parsedOrder();
    return parsed.matchPercentage >= 50;
  });

  /** Service matching the parsed order */
  protected readonly matchedService = signal<Service | null>(null);

  constructor() {
    // Watch parsedOrder and lookup matching service
    effect(() => {
      const parsed = this.parsedOrder();
      if (parsed.platform && parsed.serviceType) {
        this.catalogService.findService(parsed.platform, parsed.serviceType).subscribe(service => {
          this.matchedService.set(service ?? null);
        });
      } else {
        this.matchedService.set(null);
      }
    });

    // Watch quickOrderService and populate input text
    effect(() => {
      const quickOrder = this.quickOrderService();
      if (quickOrder && quickOrder !== this.lastProcessedQuickOrder) {
        this.lastProcessedQuickOrder = quickOrder;
        // Format: "1k Instagram Followers"
        const text = `1k ${quickOrder.name}`;
        this.inputText.set(text);
      }
    });
  }

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

  /** Whether place order should be disabled (has validation errors) */
  protected readonly isOrderDisabled = computed<boolean>(() => {
    const data = this.orderReadyData();
    return !!data?.validationError;
  });

  /**
   * Handle input text changes
   */
  protected onInputChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.inputText.set(target.value);
  }

  /**
   * Handle input focus
   */
  protected onInputFocus(): void {
    this.isInputFocused.set(true);
  }

  /**
   * Handle input blur
   */
  protected onInputBlur(): void {
    this.isInputFocused.set(false);
  }

  /**
   * Handle input submit (Enter key)
   */
  protected onInputSubmit(): void {
    // For now, just ensure the order is parsed
    // The OrderReady component handles the actual order placement
  }

  /**
   * Handle "EXPLORE MORE" click
   * Resets platform selection to show all platforms
   * Parent (Home) handles the scrolling to services section
   */
  protected onExploreMore(): void {
    // Emit reset signal to parent - parent handles scrolling
    this.resetPlatform.emit();
  }

  /**
   * Handle "MORE PLATFORM" click
   * Parent (Home) handles the scrolling to services section
   */
  protected onMorePlatform(platform: string): void {
    // Emit platform selection to parent - parent handles scrolling
    this.selectPlatform.emit(platform.toLowerCase());
  }

  /**
   * Handle "PLACE ORDER" click
   * On home page, this redirects to signup
   */
  protected onPlaceOrder(data: OrderReadyData): void {
    // Emit for parent to handle
    this.placeOrder.emit(data);

    // On home page, redirect to signup
    this.router.navigate(['/register']);
  }

  /**
   * Handle target change from OrderReady
   * Updates the input text with the new target
   */
  protected onTargetChange(newTarget: string): void {
    const currentText = this.inputText();
    const parsed = this.parsedOrder();

    let newText: string;
    if (parsed.target) {
      // Replace old target with new target
      newText = currentText.replace(parsed.target, newTarget);
    } else {
      // Append new target to text
      newText = `${currentText} ${newTarget}`;
    }
    this.inputText.set(newText);
  }

  /**
   * Handle quantity change from OrderReady
   * Updates the input text with the new quantity
   */
  protected onQuantityChange(newQuantity: number): void {
    const currentText = this.inputText();

    // Format new quantity (use k/m suffix for readability)
    const formatted = this.formatQuantity(newQuantity);

    // Replace old quantity in text
    const quantityPattern = /\d+(?:\.\d+)?\s*(k|m)?/i;
    const newText = currentText.replace(quantityPattern, formatted);
    this.inputText.set(newText);
  }

  /**
   * Format quantity with k/m suffix for readability
   */
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
}
