import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Statistic item for the hero section
 */
interface HeroStat {
  value: string;
  label: string;
  color: 'success' | 'blue';
}

/**
 * HeroSection component for the home page.
 * Displays the main title, tagline, description, and key statistics.
 *
 * @example
 * <app-hero-section />
 */
@Component({
  selector: 'app-hero-section',
  templateUrl: './hero-section.html',
  styleUrl: './hero-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeroSection {
  /** Key statistics to display */
  protected readonly stats: HeroStat[] = [
    { value: '3.3M+', label: 'ORDERS DELIVERED', color: 'success' },
    { value: '99.9%', label: 'SUCCESS RATE', color: 'blue' },
    { value: '24/7', label: 'INSTANT DELIVERY', color: 'success' }
  ];
}
