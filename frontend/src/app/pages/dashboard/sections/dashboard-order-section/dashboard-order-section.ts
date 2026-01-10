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
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DashboardSectionHeader } from '../../../../components/shared/dashboard-section-header/dashboard-section-header';
import {
  OrderReady,
  type OrderReadyData,
  type QuantityValidationError
} from '../../../../components/shared/order-ready/order-ready';
import { OrderPlaced } from '../../../../components/shared/order-placed/order-placed';
import { CatalogService } from '../../../../services/catalog.service';
import { OrderService, type OrderCreateRequest, type OrderResponse } from '../../../../core/services/order.service';
import type { Service } from '../../../../models';
import type { ServiceItemData } from '../../../../components/shared/service-item-card/service-item-card';

/**
 * Parsed order data from user input
 */
interface ParsedOrder {
  quantity: number | null;
  platform: string | null;
  serviceType: string | null;
  target: string | null;
  matchPercentage: number;
}

/**
 * Order creation state
 */
type OrderState = 'idle' | 'loading' | 'success' | 'error';

/**
 * Platform keyword mappings for parsing
 */
const PLATFORM_KEYWORDS: Record<string, string> = {
  instagram: 'instagram',
  insta: 'instagram',
  ig: 'instagram',
  tiktok: 'tiktok',
  tik: 'tiktok',
  tok: 'tiktok',
  twitter: 'twitter',
  x: 'twitter',
  youtube: 'youtube',
  yt: 'youtube',
  snapchat: 'snapchat',
  snap: 'snapchat',
  facebook: 'facebook',
  fb: 'facebook',
  discord: 'discord',
  linkedin: 'linkedin'
};

/**
 * Service type keyword mappings for parsing
 */
const SERVICE_TYPE_KEYWORDS: Record<string, string> = {
  followers: 'followers',
  follower: 'followers',
  follow: 'followers',
  likes: 'likes',
  like: 'likes',
  comments: 'comments',
  comment: 'comments'
};

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

  // -------------------------------------------------------------------------
  // Outputs
  // -------------------------------------------------------------------------

  /** Emits when order is successfully created */
  readonly orderCreated = output<OrderResponse>();

  /** Emits when user needs to navigate to wallet (insufficient balance) */
  readonly navigateToWallet = output<void>();

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
    return this.parseInput(this.inputText());
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
        platform: this.getPlatformDisplayName(parsed.platform ?? ''),
        type: this.getServiceTypeDisplayName(parsed.serviceType ?? ''),
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
    // Watch parsedOrder and lookup matching service
    effect(() => {
      const parsed = this.parsedOrder();
      if (parsed.platform && parsed.serviceType) {
        this.catalogService.findService(parsed.platform, parsed.serviceType)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe(service => {
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
        // Clear any previous messages
        this.errorMessage.set(null);
        this.successMessage.set(null);
      }
    });
  }

  // -------------------------------------------------------------------------
  // Input Parsing
  // -------------------------------------------------------------------------

  /**
   * Parse user input to extract order details
   */
  private parseInput(input: string): ParsedOrder {
    const text = input.toLowerCase().trim();

    if (!text) {
      return {
        quantity: null,
        platform: null,
        serviceType: null,
        target: null,
        matchPercentage: 0
      };
    }

    const quantity = this.extractQuantity(text);
    const platform = this.extractPlatform(text);
    const serviceType = this.extractServiceType(text);
    const target = this.extractTarget(input);

    let matchPercentage = 0;
    if (quantity) matchPercentage += 25;
    if (platform) matchPercentage += 25;
    if (serviceType) matchPercentage += 25;
    if (target) matchPercentage += 18;

    if (quantity && platform && serviceType) {
      matchPercentage = Math.min(matchPercentage + 7, 100);
    }

    return { quantity, platform, serviceType, target, matchPercentage };
  }

  /**
   * Extract quantity from text
   * Supports: 1000, 1k, 50k, 1m, 1.5k
   * Excludes numbers that are part of URLs or @usernames
   */
  private extractQuantity(text: string): number | null {
    // Remove URLs and usernames before extracting quantity
    // This prevents matching numbers in @naruto2 or instagram.com/12489510248
    let cleanText = text;

    // Remove URLs with protocol (https://instagram.com/12489510248/12903)
    cleanText = cleanText.replace(/https?:\/\/[^\s]+/gi, '');

    // Remove URLs without protocol (instagram.com/naruto2, www.example.com/path)
    cleanText = cleanText.replace(
      /(?:www\.)?[\w-]+\.(?:com|net|org|io|co|me|tv|app|dev|link|bio|page)(?:\/[^\s]*)?/gi,
      ''
    );

    // Remove @usernames (@naruto2, @user.name)
    cleanText = cleanText.replace(/@[\w.]+/g, '');

    // Now extract quantity from clean text
    // Match patterns like: 1000, 1k, 50k, 1.5k, 1m
    const match = cleanText.match(/(\d+(?:\.\d+)?)\s*(k|m)?/i);

    if (!match) return null;

    let value = parseFloat(match[1]);
    const suffix = match[2]?.toLowerCase();

    if (suffix === 'k') value *= 1000;
    else if (suffix === 'm') value *= 1000000;

    return Math.round(value);
  }

  private extractPlatform(text: string): string | null {
    const words = text.split(/\s+/);
    for (const word of words) {
      const platform = PLATFORM_KEYWORDS[word];
      if (platform) return platform;
    }
    for (const [keyword, platform] of Object.entries(PLATFORM_KEYWORDS)) {
      if (text.includes(keyword)) return platform;
    }
    return null;
  }

  private extractServiceType(text: string): string | null {
    const words = text.split(/\s+/);
    for (const word of words) {
      const serviceType = SERVICE_TYPE_KEYWORDS[word];
      if (serviceType) return serviceType;
    }
    for (const [keyword, serviceType] of Object.entries(SERVICE_TYPE_KEYWORDS)) {
      if (text.includes(keyword)) return serviceType;
    }
    return null;
  }

  private extractTarget(text: string): string | null {
    const usernameMatch = text.match(/@[\w.]+/);
    if (usernameMatch) return usernameMatch[0];

    const urlWithProtocol = text.match(/https?:\/\/[^\s]+/i);
    if (urlWithProtocol) return urlWithProtocol[0];

    const urlWithoutProtocol = text.match(
      /(?:www\.)?[\w-]+\.(?:com|net|org|io|co|me|tv|app|dev|link|bio|page)(?:\/[^\s]*)?/i
    );
    if (urlWithoutProtocol) return urlWithoutProtocol[0];

    return null;
  }

  private getPlatformDisplayName(slug: string): string {
    const names: Record<string, string> = {
      instagram: 'INSTAGRAM',
      tiktok: 'TIKTOK',
      twitter: 'TWITTER/X',
      youtube: 'YOUTUBE',
      snapchat: 'SNAPCHAT',
      facebook: 'FACEBOOK',
      discord: 'DISCORD',
      linkedin: 'LINKEDIN'
    };
    return names[slug] ?? slug.toUpperCase();
  }

  private getServiceTypeDisplayName(slug: string): string {
    const names: Record<string, string> = {
      followers: 'Followers',
      likes: 'Likes',
      comments: 'Comments'
    };
    return names[slug] ?? slug.charAt(0).toUpperCase() + slug.slice(1);
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
   */
  protected onPlaceOrder(data: OrderReadyData): void {
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

    // Create order request
    const request: OrderCreateRequest = {
      serviceId: service.id,
      target: parsed.target,
      quantity: parsed.quantity
    };

    this.orderState.set('loading');
    this.errorMessage.set(null);

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
   * Navigate to wallet to add funds
   */
  protected onAddFundsClick(): void {
    this.navigateToWallet.emit();
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
