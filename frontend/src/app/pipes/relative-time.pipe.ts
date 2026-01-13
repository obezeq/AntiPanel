import { Pipe, PipeTransform } from '@angular/core';

/**
 * Time unit configuration for relative time calculation
 */
interface TimeUnit {
  /** Number of seconds in this unit */
  seconds: number;
  /** Singular form of the unit name */
  singular: string;
  /** Plural form of the unit name */
  plural: string;
}

/**
 * Available time units from largest to smallest
 */
const TIME_UNITS: TimeUnit[] = [
  { seconds: 31536000, singular: 'year', plural: 'years' },
  { seconds: 2592000, singular: 'month', plural: 'months' },
  { seconds: 604800, singular: 'week', plural: 'weeks' },
  { seconds: 86400, singular: 'day', plural: 'days' },
  { seconds: 3600, singular: 'hour', plural: 'hours' },
  { seconds: 60, singular: 'minute', plural: 'minutes' },
  { seconds: 1, singular: 'second', plural: 'seconds' }
];

/**
 * RelativeTimePipe
 *
 * Transforms a Date or timestamp into a human-readable relative time string.
 * Supports both past and future dates.
 *
 * @example Basic usage
 * ```html
 * {{ order.createdAt | relativeTime }}
 * <!-- Output: "5 minutes ago" -->
 *
 * {{ futureDate | relativeTime }}
 * <!-- Output: "in 3 days" -->
 * ```
 *
 * @example With string input
 * ```html
 * {{ '2024-01-15T10:30:00Z' | relativeTime }}
 * <!-- Output: "2 months ago" -->
 * ```
 *
 * @example Edge cases
 * ```html
 * {{ now | relativeTime }}
 * <!-- Output: "just now" -->
 *
 * {{ null | relativeTime }}
 * <!-- Output: "" -->
 * ```
 */
@Pipe({
  name: 'relativeTime',
  standalone: true,
  pure: true
})
export class RelativeTimePipe implements PipeTransform {
  /**
   * Threshold in seconds for "just now" output
   */
  private static readonly JUST_NOW_THRESHOLD = 10;

  /**
   * Transforms a date value into a relative time string.
   *
   * @param value - Date object, ISO string, or timestamp
   * @returns Human-readable relative time string
   */
  transform(value: Date | string | number | null | undefined): string {
    if (value === null || value === undefined) {
      return '';
    }

    const date = this.parseDate(value);
    if (!date || isNaN(date.getTime())) {
      return '';
    }

    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    // Handle "just now" for very recent times
    if (Math.abs(diffInSeconds) < RelativeTimePipe.JUST_NOW_THRESHOLD) {
      return 'just now';
    }

    // Determine if past or future
    const isPast = diffInSeconds > 0;
    const absoluteDiff = Math.abs(diffInSeconds);

    // Find the appropriate time unit
    for (const unit of TIME_UNITS) {
      const count = Math.floor(absoluteDiff / unit.seconds);
      if (count >= 1) {
        const unitName = count === 1 ? unit.singular : unit.plural;
        return isPast
          ? `${count} ${unitName} ago`
          : `in ${count} ${unitName}`;
      }
    }

    return 'just now';
  }

  /**
   * Parses various date formats into a Date object.
   *
   * @param value - Date value to parse
   * @returns Parsed Date object or null
   */
  private parseDate(value: Date | string | number): Date | null {
    if (value instanceof Date) {
      return value;
    }

    if (typeof value === 'number') {
      // Handle both seconds and milliseconds timestamps
      const timestamp = value < 1e12 ? value * 1000 : value;
      return new Date(timestamp);
    }

    if (typeof value === 'string') {
      const parsed = new Date(value);
      return isNaN(parsed.getTime()) ? null : parsed;
    }

    return null;
  }
}
