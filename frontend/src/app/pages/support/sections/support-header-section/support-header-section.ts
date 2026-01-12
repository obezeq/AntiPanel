import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Support header section component.
 * Displays the "SUPPORT" title with a subtle white glow effect
 * and a description text below.
 *
 * Uses semantic HTML with proper ARIA attributes for accessibility.
 */
@Component({
  selector: 'app-support-header-section',
  templateUrl: './support-header-section.html',
  styleUrl: './support-header-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SupportHeaderSection {}
