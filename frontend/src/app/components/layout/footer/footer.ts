import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { RouterLink } from '@angular/router';

interface FooterLink {
  label: string;
  path: string;
  external?: boolean;
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

  protected readonly currentYear = new Date().getFullYear();

  protected readonly links: FooterLink[] = [
    { label: 'Terms of Service', path: '/terms' },
    { label: 'Support', path: '/support' }
  ];

  protected scrollToTop(): void {
    this.document.defaultView?.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }
}
