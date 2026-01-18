import { Injectable, inject } from '@angular/core';
import { ORDER_KEYWORDS, DISPLAY_NAMES } from '../config/order-keywords.config';
import type { ParsedOrder } from '../models/order-parser.models';

/**
 * Service for parsing natural language order input.
 * Supports multilingual keywords (English, Spanish, German, French).
 *
 * @example
 * ```typescript
 * const parser = inject(OrderParserService);
 * const result = parser.parse('1k instagram followers @username');
 * // { quantity: 1000, platform: 'instagram', serviceType: 'followers', target: '@username', matchPercentage: 100 }
 *
 * // Spanish input
 * const result2 = parser.parse('1k instagram seguidores @username');
 * // Same result - 'seguidores' maps to 'followers'
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class OrderParserService {
  private readonly keywords = inject(ORDER_KEYWORDS);
  private readonly displayNames = inject(DISPLAY_NAMES);

  /**
   * Parse user input to extract order details.
   * Supports multilingual keywords mapped to canonical English identifiers.
   *
   * @param input Raw user input string
   * @returns Parsed order with extracted fields and match percentage
   */
  parse(input: string): ParsedOrder {
    const text = input.toLowerCase().trim();

    if (!text) {
      return this.createEmptyResult();
    }

    const quantity = this.extractQuantity(text);
    const platform = this.extractPlatform(text);
    const serviceType = this.extractServiceType(text);
    const target = this.extractTarget(input); // Use original case for target

    const matchPercentage = this.calculateMatchPercentage(quantity, platform, serviceType, target);

    return { quantity, platform, serviceType, target, matchPercentage };
  }

  /**
   * Get display name for a platform slug.
   * @param slug Platform slug (e.g., 'instagram')
   * @returns Display name (e.g., 'INSTAGRAM')
   */
  getPlatformDisplayName(slug: string): string {
    return this.displayNames.platforms[slug] ?? slug.toUpperCase();
  }

  /**
   * Get display name for a service type slug.
   * @param slug Service type slug (e.g., 'followers')
   * @returns Display name (e.g., 'Followers')
   */
  getServiceTypeDisplayName(slug: string): string {
    return this.displayNames.serviceTypes[slug] ?? this.capitalize(slug);
  }

  /**
   * Extract quantity from text.
   * Supports: 1000, 1k, 50k, 1m, 1.5k
   * Excludes numbers in URLs or @usernames.
   */
  private extractQuantity(text: string): number | null {
    let cleanText = text;

    // Remove URLs with protocol
    cleanText = cleanText.replace(/https?:\/\/[^\s]+/gi, '');

    // Remove URLs without protocol
    cleanText = cleanText.replace(
      /(?:www\.)?[\w-]+\.(?:com|net|org|io|co|me|tv|app|dev|link|bio|page)(?:\/[^\s]*)?/gi,
      ''
    );

    // Remove @usernames
    cleanText = cleanText.replace(/@[\w.]+/g, '');

    const match = cleanText.match(/(\d+(?:\.\d+)?)\s*(k|m)?/i);

    if (!match) return null;

    let value = parseFloat(match[1]);
    const suffix = match[2]?.toLowerCase();

    if (suffix === 'k') value *= 1000;
    else if (suffix === 'm') value *= 1000000;

    return Math.round(value);
  }

  /**
   * Extract platform from text using keyword mapping.
   */
  private extractPlatform(text: string): string | null {
    // Check exact word matches first
    const words = text.split(/\s+/);
    for (const word of words) {
      const platform = this.keywords.platforms[word];
      if (platform) return platform;
    }

    // Check partial matches (sorted by length descending for longer matches first)
    const sortedKeywords = Object.entries(this.keywords.platforms)
      .sort(([a], [b]) => b.length - a.length);

    for (const [keyword, platform] of sortedKeywords) {
      if (text.includes(keyword)) return platform;
    }

    return null;
  }

  /**
   * Extract service type from text using keyword mapping.
   * Handles compound types like "company followers".
   */
  private extractServiceType(text: string): string | null {
    const lowerText = text.toLowerCase();

    // Check compound types FIRST
    if (lowerText.includes('company') && lowerText.includes('followers')) {
      return 'company-followers';
    }
    if (lowerText.includes('profile') && lowerText.includes('followers')) {
      return 'followers';
    }

    // Check multi-word keywords first (sorted by length descending)
    const sortedKeywords = Object.entries(this.keywords.serviceTypes)
      .sort(([a], [b]) => b.length - a.length);

    for (const [keyword, serviceType] of sortedKeywords) {
      // For multi-word keywords, check inclusion
      if (keyword.includes(' ') && lowerText.includes(keyword)) {
        return serviceType;
      }
    }

    // Check exact word matches
    const words = text.split(/\s+/);
    for (const word of words) {
      const serviceType = this.keywords.serviceTypes[word];
      if (serviceType) return serviceType;
    }

    // Check partial matches for single-word keywords
    for (const [keyword, serviceType] of sortedKeywords) {
      if (!keyword.includes(' ') && lowerText.includes(keyword)) {
        return serviceType;
      }
    }

    return null;
  }

  /**
   * Extract target (@username or URL) from text.
   * Priority: Full URL > URL without protocol > @username
   */
  private extractTarget(text: string): string | null {
    // Priority 1: URL with protocol
    const urlWithProtocol = text.match(/https?:\/\/[^\s]+/i);
    if (urlWithProtocol) return urlWithProtocol[0];

    // Priority 2: URL without protocol
    const urlWithoutProtocol = text.match(
      /(?:www\.)?[\w-]+\.(?:com|net|org|io|co|me|tv|app|dev|link|bio|page)(?:\/[^\s]*)?/i
    );
    if (urlWithoutProtocol) return urlWithoutProtocol[0];

    // Priority 3: @username
    const usernameMatch = text.match(/@[\w.]+/);
    if (usernameMatch) return usernameMatch[0];

    return null;
  }

  /**
   * Calculate match percentage based on extracted fields.
   */
  private calculateMatchPercentage(
    quantity: number | null,
    platform: string | null,
    serviceType: string | null,
    target: string | null
  ): number {
    let percentage = 0;
    if (quantity) percentage += 25;
    if (platform) percentage += 25;
    if (serviceType) percentage += 25;
    if (target) percentage += 18;

    // Bonus for having all main fields
    if (quantity && platform && serviceType) {
      percentage = Math.min(percentage + 7, 100);
    }

    return percentage;
  }

  private createEmptyResult(): ParsedOrder {
    return {
      quantity: null,
      platform: null,
      serviceType: null,
      target: null,
      matchPercentage: 0
    };
  }

  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }
}
