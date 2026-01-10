import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NgIcon } from '@ng-icons/core';

/**
 * Support coming soon section component.
 * Displays an elegant "coming soon" teaser for the ticket support feature.
 *
 * Uses semantic HTML with proper ARIA attributes.
 * Features subtle animations and visual hierarchy.
 */
@Component({
  selector: 'app-support-coming-soon-section',
  templateUrl: './support-coming-soon-section.html',
  styleUrl: './support-coming-soon-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class SupportComingSoonSection {}
