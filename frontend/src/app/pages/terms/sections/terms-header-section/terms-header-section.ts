import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Terms header section component.
 * Displays the "TERMS" title with a subtle white glow effect
 * and a description text below.
 *
 * Uses semantic HTML with proper ARIA attributes for accessibility.
 */
@Component({
  selector: 'app-terms-header-section',
  templateUrl: './terms-header-section.html',
  styleUrl: './terms-header-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TermsHeaderSection {}
