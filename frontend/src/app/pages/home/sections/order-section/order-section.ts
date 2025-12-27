import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  output,
  signal
} from '@angular/core';
import { Router } from '@angular/router';
import { DashboardSectionHeader } from '../../../../components/shared/dashboard-section-header/dashboard-section-header';
import { OrderReady, type OrderReadyData } from '../../../../components/shared/order-ready/order-ready';
import { CatalogService } from '../../../../services/catalog.service';
import type { Service } from '../../../../models';

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

  /** Emits when user wants to place an order (redirects to signup on home) */
  readonly placeOrder = output<OrderReadyData>();

  /** Current user input text */
  protected readonly inputText = signal<string>('');

  /** Whether the input field is focused */
  protected readonly isInputFocused = signal<boolean>(false);

  /** Parsed order from input text */
  protected readonly parsedOrder = computed<ParsedOrder>(() => {
    return this.parseInput(this.inputText());
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
  }

  /** Order ready data for the OrderReady component */
  protected readonly orderReadyData = computed<OrderReadyData | null>(() => {
    const parsed = this.parsedOrder();
    const service = this.matchedService();

    if (!service || !parsed.quantity) {
      return null;
    }

    const price = (service.pricePerK * parsed.quantity) / 1000;

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
      price: `$${price.toFixed(2)}`,
      target: parsed.target ?? undefined
    };
  });

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

    // Extract quantity (numbers like 1000, 1k, 50k, 1m)
    const quantity = this.extractQuantity(text);

    // Extract platform
    const platform = this.extractPlatform(text);

    // Extract service type
    const serviceType = this.extractServiceType(text);

    // Extract target (@username or URL)
    const target = this.extractTarget(input);

    // Calculate match percentage (25% for each field)
    let matchPercentage = 0;
    if (quantity) matchPercentage += 25;
    if (platform) matchPercentage += 25;
    if (serviceType) matchPercentage += 25;
    if (target) matchPercentage += 18; // Target gives less weight

    // Add bonus for having all main fields
    if (quantity && platform && serviceType) {
      matchPercentage = Math.min(matchPercentage + 7, 100);
    }

    return {
      quantity,
      platform,
      serviceType,
      target,
      matchPercentage
    };
  }

  /**
   * Extract quantity from text
   * Supports: 1000, 1k, 50k, 1m, 1.5k
   */
  private extractQuantity(text: string): number | null {
    // Match patterns like: 1000, 1k, 50k, 1.5k, 1m
    const match = text.match(/(\d+(?:\.\d+)?)\s*(k|m)?/i);

    if (!match) return null;

    let value = parseFloat(match[1]);
    const suffix = match[2]?.toLowerCase();

    if (suffix === 'k') {
      value *= 1000;
    } else if (suffix === 'm') {
      value *= 1000000;
    }

    return Math.round(value);
  }

  /**
   * Extract platform from text
   */
  private extractPlatform(text: string): string | null {
    const words = text.split(/\s+/);

    for (const word of words) {
      const platform = PLATFORM_KEYWORDS[word];
      if (platform) return platform;
    }

    // Also check for partial matches
    for (const [keyword, platform] of Object.entries(PLATFORM_KEYWORDS)) {
      if (text.includes(keyword)) return platform;
    }

    return null;
  }

  /**
   * Extract service type from text
   */
  private extractServiceType(text: string): string | null {
    const words = text.split(/\s+/);

    for (const word of words) {
      const serviceType = SERVICE_TYPE_KEYWORDS[word];
      if (serviceType) return serviceType;
    }

    // Also check for partial matches
    for (const [keyword, serviceType] of Object.entries(SERVICE_TYPE_KEYWORDS)) {
      if (text.includes(keyword)) return serviceType;
    }

    return null;
  }

  /**
   * Extract target (@username or URL) from text
   */
  private extractTarget(text: string): string | null {
    // Match @username pattern
    const usernameMatch = text.match(/@[\w.]+/);
    if (usernameMatch) return usernameMatch[0];

    // Match URL pattern
    const urlMatch = text.match(/https?:\/\/[^\s]+/i);
    if (urlMatch) return urlMatch[0];

    return null;
  }

  /**
   * Get display name for platform
   */
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

  /**
   * Get display name for service type
   */
  private getServiceTypeDisplayName(slug: string): string {
    const names: Record<string, string> = {
      followers: 'Followers',
      likes: 'Likes',
      comments: 'Comments'
    };
    return names[slug] ?? slug.charAt(0).toUpperCase() + slug.slice(1);
  }

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
   */
  protected onExploreMore(): void {
    // Scroll to services section
    const servicesSection = document.getElementById('services-section');
    servicesSection?.scrollIntoView({ behavior: 'smooth' });
  }

  /**
   * Handle "MORE PLATFORM" click
   */
  protected onMorePlatform(platform: string): void {
    // Scroll to services section and potentially filter by platform
    const servicesSection = document.getElementById('services-section');
    servicesSection?.scrollIntoView({ behavior: 'smooth' });
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
}
