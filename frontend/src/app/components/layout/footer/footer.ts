import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { RouterLink } from '@angular/router';

export type FooterVariant = 'default' | 'admin';

interface FooterLink {
  label: string;
  path: string;
  external?: boolean;
  showIn: FooterVariant[];
}

@Component({
  selector: 'app-footer',
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink]
})
export class Footer {
  private readonly document = inject(DOCUMENT);

  /** Footer variant: 'default' shows all links, 'admin' shows only Support */
  readonly variant = input<FooterVariant>('default');

  protected readonly currentYear = new Date().getFullYear();

  private readonly allLinks: FooterLink[] = [
    { label: 'TERMS OF SERVICE', path: '/terms', showIn: ['default'] },
    { label: 'SUPPORT', path: '/support', showIn: ['default', 'admin'] }
  ];

  /** Filtered links based on variant */
  protected readonly links = computed(() =>
    this.allLinks.filter(link => link.showIn.includes(this.variant()))
  );

  protected scrollToTop(): void {
    this.document.defaultView?.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }
}
