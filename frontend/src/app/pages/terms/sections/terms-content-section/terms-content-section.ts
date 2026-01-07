import { ChangeDetectionStrategy, Component } from '@angular/core';

/**
 * Terms content section component.
 * Displays all terms of service in a bordered container.
 *
 * Contains the following sections:
 * - Disclaimer
 * - Privacy Policy
 * - Refund Policy
 * - Services
 * - Customer Responsibilities
 * - Final liability statement
 *
 * Uses semantic HTML with articles for each term section.
 */
@Component({
  selector: 'app-terms-content-section',
  templateUrl: './terms-content-section.html',
  styleUrl: './terms-content-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TermsContentSection {}
