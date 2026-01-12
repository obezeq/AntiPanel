import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NgIcon } from '@ng-icons/core';

interface ContactOption {
  id: string;
  icon: string;
  title: string;
  description: string;
  action: string;
  href: string;
  ariaLabel: string;
}

/**
 * Support contact section component.
 * Displays contact option cards for Telegram and Email.
 *
 * Features Awwwards-quality interactive cards with:
 * - Subtle glow on hover
 * - Smooth transitions
 * - Accessible keyboard navigation
 */
@Component({
  selector: 'app-support-contact-section',
  templateUrl: './support-contact-section.html',
  styleUrl: './support-contact-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class SupportContactSection {
  protected readonly contactOptions: ContactOption[] = [
    {
      id: 'telegram',
      icon: 'iconoirTelegram',
      title: 'Telegram',
      description: 'Get instant support via our Telegram channel. Fast responses, 24/7.',
      action: 'Open Telegram',
      href: 'https://t.me/antipanelsupport',
      ariaLabel: 'Contact support via Telegram (opens in new tab)'
    },
    {
      id: 'email',
      icon: 'matEmail',
      title: 'Email',
      description: 'Send us a detailed message. We typically respond within 24 hours.',
      action: 'Send Email',
      href: 'mailto:support@antipanel.com',
      ariaLabel: 'Send email to support (opens email client)'
    }
  ];
}
